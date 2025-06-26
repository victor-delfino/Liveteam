import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.OutputStream;

public class PDFGenerator {

    public static void gerarPDF(OutputStream out, String conteudo) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                contentStream.newLineAtOffset(50, 700);
                contentStream.setLeading(20f);

                // Adiciona título
                contentStream.showText("Resposta Gerada");
                contentStream.newLine();

                // Adiciona o conteúdo
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.showText(conteudo);

                contentStream.endText();
            }

            // Salva o documento no OutputStream
            document.save(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
