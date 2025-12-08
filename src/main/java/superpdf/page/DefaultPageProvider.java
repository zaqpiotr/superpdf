package superpdf.page;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * Default implementation of {@link PageProvider} for managing PDF pages.
 * <p>
 * This class provides standard page management functionality including creating,
 * navigating, and retrieving pages within a PDF document with a specified page size.
 * </p>
 */
public class DefaultPageProvider implements PageProvider<PDPage> {

	private final PDDocument document;

	private final PDRectangle size;

	private int currentPageIndex = -1;

	/**
	 * Constructs a DefaultPageProvider with the specified PDF document and page size.
	 *
	 * @param document the {@link PDDocument} to manage
	 * @param size the {@link PDRectangle} page size for new pages
	 */
	public DefaultPageProvider(final PDDocument document, final PDRectangle size) {
		this.document = document;
		this.size = size;
	}

	@Override
	public PDDocument getDocument() {
		return document;
	}

	@Override
	public PDPage createPage() {
		currentPageIndex = document.getNumberOfPages();
		return getCurrentPage();
	}

	@Override
	public PDPage nextPage() {
		if (currentPageIndex == -1) {
			currentPageIndex = document.getNumberOfPages();
		} else {
			currentPageIndex++;
		}

		return getCurrentPage();
	}

	@Override
	public PDPage previousPage() {
		currentPageIndex--;
		if (currentPageIndex < 0) {
			currentPageIndex = 0;
		}

		return getCurrentPage();
	}

	private PDPage getCurrentPage() {
		if (currentPageIndex >= document.getNumberOfPages()) {
			final PDPage newPage = new PDPage(size);
			document.addPage(newPage);
			return newPage;
		}

		return document.getPage(currentPageIndex);
	}

}
