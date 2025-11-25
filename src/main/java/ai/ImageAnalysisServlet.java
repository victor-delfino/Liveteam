package ai;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/ImageAnalysisServlet")
@MultipartConfig
public class ImageAnalysisServlet extends HttpServlet {
    private static final String API_KEY = "AIzaSyDT0mk-zOEp2UVX7eWUmDenkvJZVwJTxdA";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    @Override
    public void init(jakarta.servlet.ServletConfig config) throws ServletException {
        super.init(config);
        try {
            trustAllCertificates();  // Configura SSL para confiar em todos os certificados (INSEGURO!)
            System.out.println("Certificados SSL configurados para confiar em todos (INSEGURO)!");
        } catch (Exception e) {
            System.err.println("Falha ao configurar TrustAllCertificates: " + e.getMessage());
            throw new ServletException("Falha ao inicializar o servlet", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        File tempFile = null;

        try {
            Part filePart = request.getPart("image");
            if (filePart == null || filePart.getSize() == 0) {
                throw new ServletException("Nenhum arquivo enviado.");
            }

            String contentType = filePart.getContentType();
            if (!"image/jpeg".equals(contentType)) {
                throw new ServletException("Apenas imagens JPEG são suportadas.");
            }

            String fileName = "upload_" + System.currentTimeMillis() + ".jpg";
            Path tempDirPath = Path.of(System.getProperty("java.io.tmpdir"));
            Path tempFilePath = tempDirPath.resolve(fileName);

            try (InputStream fileContent = filePart.getInputStream()) {
                Files.copy(fileContent, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
                tempFile = tempFilePath.toFile();
            }

            String base64Image = encodeImage(tempFile.getAbsolutePath());

            String prompt = "Observe a imagem de alimentos enviada. Retorne somente uma lista de itens no seguinte formato:\n" +
                    "\n" +
                    "<nome do alimento> (<quantidade estimada em gramas>) – <calorias em número> kcal\n" +
                    "\n" +
                    "Exemplo:\n" +
                    "- Banana (<quantidade estimada em gramas Em numeros>) – 89 kcal  \n" +
                    "- Iogurte natural (<quantidade estimada em gramas Em numeros>) – 120 kcal  \n" +
                    "- Granola (<quantidade estimada em gramas Em numeros>) – 95 kcal\n" +
                    "\n" +
                    "Não adicione nenhum comentário ou explicação. Apenas a lista no formato acima.";

            String aiResponse = sendRequest(base64Image, prompt);

            JSONObject jsonParsed = parseFoodList(aiResponse);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonParsed.toString());

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject errorJson = new JSONObject().put("response", "Erro ao processar imagem: " + e.getMessage());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(errorJson.toString());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    System.err.println("Falha ao deletar arquivo temporário: " + tempFile.getAbsolutePath());
                }
            }
        }
    }

    private String encodeImage(String imagePath) throws IOException {
        byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private String sendRequest(String base64Image, String prompt) throws Exception {
        JSONObject inlineData = new JSONObject()
                .put("mime_type", "image/jpeg")
                .put("data", base64Image);

        JSONArray parts = new JSONArray()
                .put(new JSONObject().put("text", prompt))
                .put(new JSONObject().put("inline_data", inlineData));

        JSONObject content = new JSONObject().put("parts", parts);
        JSONObject payload = new JSONObject().put("contents", new JSONArray().put(content));

        URL url = new URL(API_URL);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        trustAllCertificates();

        connection.setHostnameVerifier((hostname, session) -> true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = payload.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int status = connection.getResponseCode();
        InputStream responseStream = (status >= 200 && status < 300)
                ? connection.getInputStream()
                : connection.getErrorStream();

        String responseText = new String(responseStream.readAllBytes(), "utf-8");

        if (status != 200) {
            throw new RuntimeException("Erro na API: " + status + " - " + responseText);
        }

        JSONObject responseJson = new JSONObject(responseText);
        JSONArray candidates = responseJson.optJSONArray("candidates");

        if (candidates != null && candidates.length() > 0) {
            JSONObject firstCandidate = candidates.getJSONObject(0);
            JSONObject contentObj = firstCandidate.optJSONObject("content");
            if (contentObj != null) {
                JSONArray partsArray = contentObj.optJSONArray("parts");
                if (partsArray != null && partsArray.length() > 0) {
                    return partsArray.getJSONObject(0).getString("text");
                }
            }
        }

        return "Nenhuma descrição encontrada.";
    }

    private JSONObject parseFoodList(String aiResponse) {
        JSONObject result = new JSONObject();
        JSONArray itemsArray = new JSONArray();

        String[] lines = aiResponse.split("\\r?\\n");

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty() || !line.startsWith("-")) continue;

            line = line.substring(1).trim();
            line = line.replace("–", "-");

            String regex = "^(.*)\\s*\\(([^)]*)\\)\\s*-\\s*(\\d+)\\s*kcal$";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(line);

            if (matcher.find()) {
                String alimento = matcher.group(1).trim();
                String quantidade = matcher.group(2).trim();
                int calorias = Integer.parseInt(matcher.group(3));

                JSONObject item = new JSONObject();
                item.put("alimento", alimento);
                item.put("quantidade", quantidade);
                item.put("calorias", calorias);

                itemsArray.put(item);
            }
        }

        result.put("items", itemsArray);
        return result;
    }

    private void trustAllCertificates() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }
}
