package superpdf.line;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Objects;

/**
 * The <code>LineStyle</code> class defines a basic set of rendering attributes
 * for lines.
 */
public class LineStyle {

	private final Color color;

	private final float width;

	private float[] dashArray;

	private float dashPhase;

	/**
	 * Constructs a LineStyle with the specified color and width.
	 * This creates a solid line without dashing or dotting.
	 *
	 * @param color the {@link Color} of the line
	 * @param width the width of the line in points
	 */
	public LineStyle(final Color color, final float width) {
		this.color = color;
		this.width = width;
	}

	/**
	 * <p>
	 * Provides ability to produce dotted line.
	 * </p>
	 *
	 * @param color
	 *            The {@link Color} of the line
	 * @param width
	 *            The line width
	 * @return new styled line
	 */
	public static LineStyle produceDotted(final Color color, final int width) {
		final LineStyle line = new LineStyle(color, width);
		line.dashArray = new float[] { 1.0f };
		line.dashPhase = 0.0f;

		return line;
	}

	/**
	 * <p>
	 * Provides ability to produce dashed line.
	 * </p>
	 *
	 * @param color
	 *            The {@link Color} of the line
	 * @param width
	 *            The line width
	 * @return new styled line
	 */
	public static LineStyle produceDashed(final Color color, final int width) {
		return produceDashed(color, width, new float[] { 5.0f }, 0.0f);
	}

	/**
	 * Produces a dashed line style with specified color, width, and dash pattern.
	 *
	 * @param color
	 *            The {@link Color} of the line
	 * @param width
	 *            The line width
	 * @param dashArray
	 *            Mimics the behavior of {@link BasicStroke#getDashArray()}
	 * @param dashPhase
	 *            Mimics the behavior of {@link BasicStroke#getDashPhase()}
	 * @return new styled line
	 */
	public static LineStyle produceDashed(final Color color, final int width, final float[] dashArray,
			final float dashPhase) {
		final LineStyle line = new LineStyle(color, width);
		line.dashArray = dashArray;
		line.dashPhase = dashPhase;

		return line;
	}

	/**
	 * Gets the color of this line.
	 *
	 * @return the {@link Color} of the line
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Gets the width of this line.
	 *
	 * @return the width in points
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * Gets the dash array pattern for this line.
	 * Returns null for solid lines, or an array of alternating on/off dash lengths
	 * for dashed or dotted lines. Mimics the behavior of {@link BasicStroke#getDashArray()}.
	 *
	 * @return the dash array, or null if this is a solid line
	 */
	public float[] getDashArray() {
		return dashArray;
	}

	/**
	 * Gets the dash phase offset for this line.
	 * Determines where in the dash pattern to start. Mimics the behavior of
	 * {@link BasicStroke#getDashPhase()}.
	 *
	 * @return the dash phase offset
	 */
	public float getDashPhase() {
		return dashPhase;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.color);
        hash = 89 * hash + Float.floatToIntBits(this.width);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LineStyle other = (LineStyle) obj;
        if (!Objects.equals(this.color, other.color)) {
            return false;
        }
        if (Float.floatToIntBits(this.width) != Float.floatToIntBits(other.width)) {
            return false;
        }
        return true;
    }


}
