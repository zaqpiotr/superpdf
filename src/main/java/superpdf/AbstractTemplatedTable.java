package superpdf;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;

import superpdf.page.PageProvider;

public abstract class AbstractTemplatedTable<T extends AbstractPageTemplate> extends Table<T> {

    public AbstractTemplatedTable(float yStart, float yStartNewPage, float bottomMargin, float width, float margin, PDDocument document, T currentPage, boolean drawLines, boolean drawContent, PageProvider<T> pageProvider) throws IOException {
        super(yStart, yStartNewPage, 0, bottomMargin, width, margin, document, currentPage, drawLines, drawContent, pageProvider);
    }

    public AbstractTemplatedTable(float yStartNewPage, float bottomMargin, float width, float margin, PDDocument document, boolean drawLines, boolean drawContent, PageProvider<T> pageProvider) throws IOException {
        super(yStartNewPage, 0, bottomMargin, width, margin, document, drawLines, drawContent, pageProvider);
        setYStart(getCurrentPage().yStart());
    }

}
