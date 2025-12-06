# SuperPDF

A Java library to build tables in PDF documents.

SuperPDF is a library that can be used to easily create tables in PDF documents. It uses the [PDFBox](https://pdfbox.apache.org/) PDF library under the hood.

## Features

- Build tables in PDF documents
- Convert CSV data into tables in PDF documents
- Convert Lists into tables in PDF documents

### SuperPDF supports these table features
- HTML tags in cell content (not all! `<p>,<i>,<b>,<br>,<ul>,<ol>,<li>`)
- Horizontal & Vertical Alignment of the text
- Images inside cells and outside table (image scale is also supported)
- Basic set of rendering attributes for lines (borders)
- Rotated text (by 90 degrees)
- Writing text outside tables

## Usage examples

### Create a PDF from a CSV file

```java
String data = readData("data.csv");
BaseTable pdfTable = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true, true);
DataTable t = new DataTable(pdfTable, page);
t.addCsvToTable(data, DataTable.HASHEADER, ';');
pdfTable.draw();
```

### Create a PDF from a List

```java
List<List> data = new ArrayList();
data.add(new ArrayList<>(
               Arrays.asList("Column One", "Column Two", "Column Three", "Column Four", "Column Five")));
for (int i = 1; i <= 100; i++) {
  data.add(new ArrayList<>(
      Arrays.asList("Row " + i + " Col One", "Row " + i + " Col Two", "Row " + i + " Col Three", "Row " + i + " Col Four", "Row " + i + " Col Five")));
}
BaseTable dataTable = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true, true);
DataTable t = new DataTable(dataTable, page);
t.addListToTable(data, DataTable.HASHEADER);
dataTable.draw();
```

### Build tables in PDF documents

```java
BaseTable table = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true, drawContent);
// Create Header row
Row<PDPage> headerRow = table.createRow(15f);
Cell<PDPage> cell = headerRow.createCell(100, "Header Title");
cell.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD));
cell.setFillColor(Color.BLACK);
table.addHeaderRow(headerRow);
List<String[]> facts = getFacts();
for (String[] fact : facts) {
    Row<PDPage> row = table.createRow(10f);
    cell = row.createCell((100 / 3.0f) * 2, fact[0]);
    for (int i = 1; i < fact.length; i++) {
        cell = row.createCell((100 / 9f), fact[i]);
    }
}
table.draw();
```

## License

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
