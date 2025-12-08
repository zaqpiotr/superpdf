package superpdf.page;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

/**
 * Interface for managing PDF pages within a document.
 * <p>
 * Implementations of this interface provide page management functionality,
 * allowing creation and navigation through pages in a PDF document.
 * </p>
 *
 * @param <T> the type of PDPage
 */
public interface PageProvider<T extends PDPage> {

	/**
	 * Creates a new page and adds it to the document.
	 *
	 * @return the newly created {@link PDPage}
	 */
	T createPage();

	/**
	 * Navigates to the next page in the document.
	 * If at the end of the document, a new page is created.
	 *
	 * @return the next {@link PDPage}
	 */
	T nextPage();

	/**
	 * Navigates to the previous page in the document.
	 * If already at the first page, remains on the first page.
	 *
	 * @return the previous {@link PDPage}
	 */
	T previousPage();

	/**
	 * Gets the PDF document managed by this provider.
	 *
	 * @return the {@link PDDocument}
	 */
	PDDocument getDocument();
}
