# SuperPDF

A Java library to build tables in PDF documents using Apache PDFBox.

## Installation

### Maven Central (after sync)
```xml
<dependency>
    <groupId>io.github.zaqpiotr</groupId>
    <artifactId>superpdf</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```groovy
implementation 'io.github.zaqpiotr:superpdf:1.0.0'
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

### Custom Fonts with HTML Support

```java
import static superpdf.utils.FontUtils.addDefaultFonts;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

// Load custom fonts
PDType0Font font = PDType0Font.load(document, new FileInputStream("fonts/Regular.ttf"));
PDType0Font fontBold = PDType0Font.load(document, new FileInputStream("fonts/Bold.ttf"));
PDType0Font fontItalic = PDType0Font.load(document, new FileInputStream("fonts/Italic.ttf"));
PDType0Font fontBoldItalic = PDType0Font.load(document, new FileInputStream("fonts/BoldItalic.ttf"));

// Register fonts for HTML tag support (<b>, <i>)
addDefaultFonts(font, fontBold, fontItalic, fontBoldItalic);

// Now you can use HTML in cells
cell.setText("This is <b>bold</b> and <i>italic</i> text");
```

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
