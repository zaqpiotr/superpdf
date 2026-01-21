# SuperPDF

A Java library to build tables in PDF documents using Apache PDFBox.

## Installation

### Maven Central (after sync)
```xml
<dependency>
    <groupId>com.monitglass</groupId>
    <artifactId>superpdf</artifactId>
    <version>1.1.2</version>
</dependency>
```

### Gradle
```groovy
implementation 'com.monitglass:superpdf:1.1.2'
```

## Features

- Build tables in PDF documents
- Convert CSV data into tables
- Convert Lists into tables
- HTML tags support in cells (`<b>`, `<i>`, `<br>`, `<ul>`, `<ol>`, `<li>`)
- Horizontal & Vertical alignment
- Images inside cells
- Line styles (solid, dashed, dotted)
- Custom fonts support
- Rotated text (90 degrees)

## Quick Start

### Creating a Simple Table

```java
import superpdf.BaseTable;
import superpdf.Cell;
import superpdf.Row;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

PDDocument document = new PDDocument();
PDPage page = new PDPage();
document.addPage(page);

float margin = 50;
float yStart = page.getMediaBox().getHeight() - margin;
float tableWidth = page.getMediaBox().getWidth() - (2 * margin);

BaseTable table = new BaseTable(yStart, yStart, margin, tableWidth, margin, document, page, true, true);

// Create header row
Row<PDPage> headerRow = table.createRow(15f);
Cell<PDPage> cell = headerRow.createCell(100, "Header Title");
cell.setFontSize(12);
table.addHeaderRow(headerRow);

// Create data rows
Row<PDPage> row = table.createRow(10f);
row.createCell(50, "Column 1");
row.createCell(50, "Column 2");

table.draw();
document.save("output.pdf");
document.close();
```

### Cell Styling

```java
import superpdf.HorizontalAlignment;
import superpdf.VerticalAlignment;
import superpdf.line.LineStyle;
import java.awt.Color;

Row<PDPage> row = table.createRow(20f);
Cell<PDPage> cell = row.createCell(100, "Styled Cell");

// Font settings
cell.setFont(font);
cell.setFontSize(10);
cell.setTextColor(Color.BLACK);

// Alignment
cell.setAlign(HorizontalAlignment.CENTER);
cell.setValign(VerticalAlignment.MIDDLE);

// Background
cell.setFillColor(new Color(240, 240, 240));

// Padding
cell.setTopPadding(5f);
cell.setBottomPadding(5f);
cell.setLeftPadding(5f);
cell.setRightPadding(5f);

// Borders
cell.setBorderStyle(new LineStyle(Color.BLACK, 1f));
cell.setBottomBorderStyle(new LineStyle(Color.BLACK, 1f));
cell.setTopBorderStyle(new LineStyle(Color.BLACK, 1f));
```

### Line Styles

```java
import superpdf.line.LineStyle;

// Solid line
LineStyle solid = new LineStyle(Color.BLACK, 1f);

// Dashed line
LineStyle dashed = LineStyle.produceDashed(Color.BLACK, 1);

// Dotted line
LineStyle dotted = LineStyle.produceDotted(Color.BLACK, 1);

// Custom dash pattern
LineStyle custom = LineStyle.produceDashed(Color.GRAY, 1, new float[]{3.0f, 2.0f}, 0.0f);

cell.setBottomBorderStyle(dashed);
```

### Built-in Noto Sans Fonts

SuperPDF includes Google's **Noto Sans** font family with full Unicode support. The name "Noto" comes from "No Tofu" - referring to the blank boxes (□) that appear when a font doesn't support a character.

```java
import static superpdf.utils.FontUtils.setNotoSansFontsAsDefault;
import static superpdf.utils.FontUtils.getDefaultfonts;
import superpdf.utils.FontScript;

// Use Latin/Greek/Cyrillic fonts (default)
setNotoSansFontsAsDefault(document, FontScript.LATIN);

// Or use Arabic script
setNotoSansFontsAsDefault(document, FontScript.ARABIC);

// Or use Hebrew script
setNotoSansFontsAsDefault(document, FontScript.HEBREW);

// Get the loaded fonts for cell styling
var fonts = getDefaultfonts();
cell.setFont(fonts.get("font"));        // Regular
cell.setFont(fonts.get("fontBold"));    // Bold
cell.setFont(fonts.get("fontItalic"));  // Italic
```

#### Available Font Scripts

| Script | Languages Supported | Italic Support |
|--------|---------------------|----------------|
| `FontScript.LATIN` | Latin, Greek, Cyrillic (Western/Eastern European, Russian) | Yes |
| `FontScript.ARABIC` | Arabic, Persian, Urdu | No* |
| `FontScript.HEBREW` | Hebrew, Yiddish | No* |

*Arabic and Hebrew scripts do not traditionally use italic styles in typography. When italic is requested, the regular font variant is used instead.

#### Extending with Additional Scripts

The Noto project covers virtually every Unicode character. Additional fonts can be downloaded from:
- [Google Fonts - Noto](https://fonts.google.com/noto)
- [GitHub - noto-fonts](https://github.com/googlefonts/noto-fonts)

Available Noto Sans variants include:
- **Noto Sans CJK** - Chinese, Japanese, Korean
- **Noto Sans Devanagari** - Hindi, Sanskrit, Marathi, Nepali
- **Noto Sans Thai** - Thai
- **Noto Sans Bengali** - Bengali, Assamese
- **Noto Sans Tamil** - Tamil
- And many more...

To add a new script, download the TTF files, add them to the `fonts/` resources directory, and add a new enum constant to `FontScript`.

### HTML Support in Cells

Once fonts are loaded, you can use HTML tags in cell content:

```java
// Load fonts first
setNotoSansFontsAsDefault(document, FontScript.LATIN);

// Now you can use HTML in cells
cell.setText("This is <b>bold</b> and <i>italic</i> text");
cell.setText("Line 1<br>Line 2");
cell.setText("<ul><li>Item 1</li><li>Item 2</li></ul>");
```

Supported HTML tags: `<b>`, `<i>`, `<br>`, `<ul>`, `<ol>`, `<li>`

### Images in Cells

```java
import superpdf.image.Image;
import superpdf.utils.PageContentStreamOptimized;

// Load image
Image image = new Image(ImageIO.read(new File("image.png")));

// Scale image
image.scaleByHeight(100f);
// or
image.scaleByWidth(150f);

// Draw image on page
PDPageContentStream contentStream = new PDPageContentStream(document, page, true, true);
image.draw(document, new PageContentStreamOptimized(contentStream), x, y);
contentStream.close();
```

### Writing Text Outside Tables

```java
import superpdf.utils.PDStreamUtils;
import superpdf.utils.PageContentStreamOptimized;

PDPageContentStream contentStream = new PDPageContentStream(document, page, true, true);
PDStreamUtils.write(
    new PageContentStreamOptimized(contentStream),
    "Footer text",
    font,
    10,    // fontSize
    50,    // x position
    30,    // y position
    Color.BLACK
);
contentStream.endText();
contentStream.close();
```

### Creating Tables from CSV

```java
import superpdf.datatable.DataTable;

BaseTable pdfTable = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true, true);
DataTable dataTable = new DataTable(pdfTable, page);

String csvData = "Name;Age;City\nJohn;30;NYC\nJane;25;LA";
dataTable.addCsvToTable(csvData, DataTable.HASHEADER, ';');

pdfTable.draw();
```

### Creating Tables from Lists

```java
import superpdf.datatable.DataTable;

List<List> data = new ArrayList<>();
data.add(Arrays.asList("Name", "Age", "City"));
data.add(Arrays.asList("John", "30", "NYC"));
data.add(Arrays.asList("Jane", "25", "LA"));

BaseTable pdfTable = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true, true);
DataTable dataTable = new DataTable(pdfTable, page);
dataTable.addListToTable(data, DataTable.HASHEADER);

pdfTable.draw();
```

## BaseTable Constructor Parameters

```java
new BaseTable(
    yStart,           // Starting Y position
    yStartNewPage,    // Y position for new pages
    bottomMargin,     // Bottom margin
    tableWidth,       // Table width
    margin,           // Left margin
    document,         // PDDocument
    page,             // PDPage
    drawLines,        // Draw table lines (true/false)
    drawContent       // Draw content (true/false)
);
```

## License

Licensed under the Apache License, Version 2.0.
