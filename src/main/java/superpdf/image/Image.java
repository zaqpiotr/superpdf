package superpdf.image;

import superpdf.utils.ImageUtils;
import superpdf.utils.PageContentStreamOptimized;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;


/**
 * Represents an image that can be drawn on a PDF document with support for scaling
 * and quality control. This class wraps a BufferedImage and provides utilities for
 * converting pixel dimensions to PDF points and scaling based on DPI values.
 */
public class Image {

	private final BufferedImage image;

	private float width;

	private float height;

	private PDImageXObject imageXObject = null;

	// standard DPI
	private float[] dpi = { 72, 72 };

	private float quality = 1f;

	/**
	 * Constructs an Image with default DPI settings (72x72).
	 *
	 * @param image the {@link BufferedImage} to wrap
	 */
	public Image(final BufferedImage image) {
		this.image = image;
		this.width = image.getWidth();
		this.height = image.getHeight();
	}

	/**
	 * Constructs an Image with uniform DPI for both X and Y axes.
	 *
	 * @param image the {@link BufferedImage} to wrap
	 * @param dpi the dots per inch to apply to both axes
	 */
	public Image(final BufferedImage image, float dpi) {
		this(image, dpi, dpi);
	}

	/**
	 * Constructs an Image with separate DPI values for X and Y axes,
	 * and scales the image dimensions from pixels to PDF points.
	 *
	 * @param image the {@link BufferedImage} to wrap
	 * @param dpiX the dots per inch for the X axis
	 * @param dpiY the dots per inch for the Y axis
	 */
	public Image(final BufferedImage image, float dpiX, float dpiY) {
		this.image = image;
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.dpi[0] = dpiX;
		this.dpi[1] = dpiY;
		scaleImageFromPixelToPoints();
	}

	/**
	 * Draws this image on a PDF page at the specified coordinates.
	 * The image is lazily converted to a PDImageXObject on the first draw call.
	 * The conversion method depends on the quality setting: lossless conversion
	 * for full quality, or JPEG compression for reduced quality.
	 *
	 * @param doc the {@link PDDocument} to which the image will be added
	 * @param stream the {@link PageContentStreamOptimized} where the image will be drawn
	 * @param x the X coordinate for image placement
	 * @param y the Y coordinate for image placement
	 * @throws IOException if the image conversion or drawing fails
	 */
	public void draw(final PDDocument doc, final PageContentStreamOptimized stream, float x, float y) throws IOException
	{
		if (imageXObject == null) {
			if(quality == 1f) {
				imageXObject = LosslessFactory.createFromImage(doc, image);
			} else {
				imageXObject = JPEGFactory.createFromImage(doc, image, quality);
			}
		}
		stream.drawImage(imageXObject, x, y - height, width, height);
	}

	/**
	 * Scales this image to fit within the specified width while maintaining
	 * aspect ratio. The height is adjusted proportionally.
	 *
	 * @param width the maximum width for the scaled image
	 * @return this {@link Image} instance for method chaining
	 */
	public Image scaleByWidth(float width) {
		float factorWidth = width / this.width;
		return scale(width, this.height * factorWidth);
	}

	private void scaleImageFromPixelToPoints() {
		float dpiX = dpi[0];
		float dpiY = dpi[1];
		scale(getImageWidthInPoints(dpiX), getImageHeightInPoints(dpiY));
	}

	/**
	 * Scales this image to fit within the specified height while maintaining
	 * aspect ratio. The width is adjusted proportionally.
	 *
	 * @param height the maximum height for the scaled image
	 * @return this {@link Image} instance for method chaining
	 */
	public Image scaleByHeight(float height) {
		float factorHeight = height / this.height;
		return scale(this.width * factorHeight, height);
	}

	/**
	 * Converts the image width from pixels to PDF points using the specified DPI.
	 * PDF uses 72 points per inch as the standard unit.
	 *
	 * @param dpiX the dots per inch for the X axis
	 * @return the image width in PDF points
	 */
	public float getImageWidthInPoints(float dpiX) {
		return this.width * 72f / dpiX;
	}

	/**
	 * Converts the image height from pixels to PDF points using the specified DPI.
	 * PDF uses 72 points per inch as the standard unit.
	 *
	 * @param dpiY the dots per inch for the Y axis
	 * @return the image height in PDF points
	 */
	public float getImageHeightInPoints(float dpiY) {
		return this.height * 72f / dpiY;
	}

	/**
	 * Scales this image to fit within the specified width and height bounds
	 * while maintaining aspect ratio. Uses {@link ImageUtils#getScaledDimension}
	 * to calculate the appropriate scaling factor.
	 *
	 * @param boundWidth the maximum width for the scaled image
	 * @param boundHeight the maximum height for the scaled image
	 * @return this {@link Image} instance for method chaining
	 */
	public Image scale(float boundWidth, float boundHeight) {
		float[] imageDimension = ImageUtils.getScaledDimension(this.width, this.height, boundWidth, boundHeight);
		this.width = imageDimension[0];
		this.height = imageDimension[1];
		return this;
	}

	/**
	 * Gets the current height of this image in PDF points.
	 *
	 * @return the image height in points
	 */
	public float getHeight() {
		return height;
	}

	/**
	 * Gets the current width of this image in PDF points.
	 *
	 * @return the image width in points
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * Sets the compression quality for JPEG encoding when drawing this image.
	 * The quality should be between 0 (exclusive) and 1 (inclusive), where
	 * 1.0 represents full quality (lossless encoding).
	 *
	 * @param quality the compression quality value between 0 and 1
	 * @throws IllegalArgumentException if quality is not in the range (0, 1]
	 */
	public void setQuality(float quality) throws IllegalArgumentException {
		if(quality <= 0 || quality > 1) {
			throw new IllegalArgumentException(
					"The quality value must be configured greater than zero and less than or equal to 1");
		}
		this.quality = quality;
	}
}
