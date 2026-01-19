package superpdf.utils;

/**
 * Defines the available font scripts for PDF generation.
 * <p>
 * Each script uses the appropriate Noto Sans font variant optimized for
 * the target writing system.
 * </p>
 */
public enum FontScript {

	/**
	 * Latin, Greek, and Cyrillic scripts.
	 * <p>
	 * Supports Western European, Eastern European, Greek, and Russian languages.
	 * Includes Regular, Bold, Italic, and BoldItalic variants.
	 * </p>
	 */
	LATIN("fonts/NotoSans-Regular.ttf", "fonts/NotoSans-Bold.ttf",
			"fonts/NotoSans-Italic.ttf", "fonts/NotoSans-BoldItalic.ttf"),

	/**
	 * Arabic script.
	 * <p>
	 * Supports Arabic, Persian, Urdu, and other languages using Arabic script.
	 * Note: Arabic script does not use italic variants; Regular is used for italic styles.
	 * </p>
	 */
	ARABIC("fonts/NotoSansArabic-Regular.ttf", "fonts/NotoSansArabic-Bold.ttf",
			"fonts/NotoSansArabic-Regular.ttf", "fonts/NotoSansArabic-Bold.ttf"),

	/**
	 * Hebrew script.
	 * <p>
	 * Supports Hebrew and Yiddish languages.
	 * Note: Hebrew script does not use italic variants; Regular is used for italic styles.
	 * </p>
	 */
	HEBREW("fonts/NotoSansHebrew-Regular.ttf", "fonts/NotoSansHebrew-Bold.ttf",
			"fonts/NotoSansHebrew-Regular.ttf", "fonts/NotoSansHebrew-Bold.ttf");

	private final String regularPath;
	private final String boldPath;
	private final String italicPath;
	private final String boldItalicPath;

	FontScript(String regularPath, String boldPath, String italicPath, String boldItalicPath) {
		this.regularPath = regularPath;
		this.boldPath = boldPath;
		this.italicPath = italicPath;
		this.boldItalicPath = boldItalicPath;
	}

	public String getRegularPath() {
		return regularPath;
	}

	public String getBoldPath() {
		return boldPath;
	}

	public String getItalicPath() {
		return italicPath;
	}

	public String getBoldItalicPath() {
		return boldItalicPath;
	}
}
