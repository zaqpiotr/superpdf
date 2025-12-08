package superpdf;

import org.apache.pdfbox.pdmodel.PDPage;

import superpdf.image.Image;

/**
 * A table cell that contains an image.
 * Extends the base Cell class to provide image-specific functionality including scaling.
 *
 * @param <T> the page type extending PDPage
 */
public class ImageCell<T extends PDPage> extends Cell<T> {

	private Image img;
	
	private final HorizontalAlignment align;
	
	private final VerticalAlignment valign;

	ImageCell(Row<T> row, float width, Image image, boolean isCalculated) {
		super(row, width, null, isCalculated);
		this.img = image;
		if(image.getWidth() > getInnerWidth()){
			scaleToFit();
		}
		this.align = HorizontalAlignment.LEFT;
		this.valign = VerticalAlignment.TOP;
	}

	/**
	 * Scales the image to fit within the cell's inner width while maintaining
	 * aspect ratio. This method adjusts the image if it exceeds the available width.
	 */
	public void scaleToFit() {
		img = img.scaleByWidth(getInnerWidth());
	}

	ImageCell(Row<T> row, float width, Image image, boolean isCalculated, HorizontalAlignment align,
			VerticalAlignment valign) {
		super(row, width, null, isCalculated, align, valign);
		this.img = image;
		if(image.getWidth() > getInnerWidth()){
			scaleToFit();
		}
		this.align = align;
		this.valign = valign;
	}

	@Override
	public float getTextHeight() {
		return img.getHeight();
	}

	@Override
	public float getHorizontalFreeSpace() {
		return getInnerWidth() - img.getWidth();
	}
	
	@Override
	public float getVerticalFreeSpace() {
		return getInnerHeight() - img.getHeight();
	}


	/**
	 * <p>
	 * Method which retrieve {@link Image}
	 * </p>
	 * 
	 * @return {@link Image}
	 */
	public Image getImage() {
		return img;
	}
}
