package superpdf

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle
import spock.lang.Specification
import superpdf.datatable.DataTable
import java.awt.Color

class DataTableSpec extends Specification {

    def "DataTable can be created from list data"() {
        given:
        def doc = new PDDocument()
        def page = new PDPage(PDRectangle.A4)
        doc.addPage(page)

        def data = [
            ["Name", "Age", "City"],
            ["Alice", "30", "New York"],
            ["Bob", "25", "Los Angeles"],
            ["Charlie", "35", "Chicago"]
        ]

        def table = new BaseTable(700, 700, 50, 500, 50, doc, page, true, true)

        when:
        def dataTable = new DataTable(table, page)
        dataTable.addListToTable(data, DataTable.HASHEADER)

        then:
        dataTable != null
        noExceptionThrown()

        cleanup:
        doc?.close()
    }

    def "DataTable can be created from CSV data"() {
        given:
        def doc = new PDDocument()
        def page = new PDPage(PDRectangle.A4)
        doc.addPage(page)

        def csv = """Name,Age,City
Alice,30,New York
Bob,25,Los Angeles
Charlie,35,Chicago"""

        def table = new BaseTable(700, 700, 50, 500, 50, doc, page, true, true)

        when:
        def dataTable = new DataTable(table, page)
        dataTable.addCsvToTable(csv, DataTable.HASHEADER, ',' as char)

        then:
        dataTable != null
        noExceptionThrown()

        cleanup:
        doc?.close()
    }

    def "DataTable header cell template can be customized"() {
        given:
        def doc = new PDDocument()
        def page = new PDPage(PDRectangle.A4)
        doc.addPage(page)

        def data = [
            ["Header1", "Header2"],
            ["Data1", "Data2"]
        ]

        def table = new BaseTable(700, 700, 50, 500, 50, doc, page, true, true)

        when:
        def dataTable = new DataTable(table, page)
        def headerTemplate = dataTable.headerCellTemplate
        headerTemplate.setFillColor(Color.BLUE)
        headerTemplate.setTextColor(Color.WHITE)
        dataTable.addListToTable(data, DataTable.HASHEADER)

        then:
        headerTemplate.fillColor == Color.BLUE
        headerTemplate.textColor == Color.WHITE

        cleanup:
        doc?.close()
    }

    def "DataTable supports zebra striping with even/odd templates"() {
        given:
        def doc = new PDDocument()
        def page = new PDPage(PDRectangle.A4)
        doc.addPage(page)

        def data = [
            ["Col1", "Col2"],
            ["Row1", "Data"],
            ["Row2", "Data"],
            ["Row3", "Data"],
            ["Row4", "Data"]
        ]

        def table = new BaseTable(700, 700, 50, 500, 50, doc, page, true, true)

        when:
        def dataTable = new DataTable(table, page)
        dataTable.addListToTable(data, DataTable.HASHEADER)

        then:
        noExceptionThrown()

        cleanup:
        doc?.close()
    }

    def "DataTable supports first and last column templates"() {
        given:
        def doc = new PDDocument()
        def page = new PDPage(PDRectangle.A4)
        doc.addPage(page)

        def data = [
            ["First", "Middle", "Last"],
            ["A", "B", "C"]
        ]

        def table = new BaseTable(700, 700, 50, 500, 50, doc, page, true, true)

        when:
        def dataTable = new DataTable(table, page)
        def firstColTemplate = dataTable.firstColumnCellTemplate
        def lastColTemplate = dataTable.lastColumnCellTemplate

        firstColTemplate.setFillColor(Color.YELLOW)
        lastColTemplate.setFillColor(Color.CYAN)

        dataTable.addListToTable(data, DataTable.HASHEADER)

        then:
        firstColTemplate.fillColor == Color.YELLOW
        lastColTemplate.fillColor == Color.CYAN

        cleanup:
        doc?.close()
    }

    def "DataTable can handle empty data"() {
        given:
        def doc = new PDDocument()
        def page = new PDPage(PDRectangle.A4)
        doc.addPage(page)

        def data = []

        def table = new BaseTable(700, 700, 50, 500, 50, doc, page, true, true)

        when:
        def dataTable = new DataTable(table, page)
        dataTable.addListToTable(data, DataTable.NOHEADER)

        then:
        noExceptionThrown()

        cleanup:
        doc?.close()
    }

    def "DataTable can handle data without headers"() {
        given:
        def doc = new PDDocument()
        def page = new PDPage(PDRectangle.A4)
        doc.addPage(page)

        def data = [
            ["Data1", "Data2", "Data3"],
            ["Data4", "Data5", "Data6"]
        ]

        def table = new BaseTable(700, 700, 50, 500, 50, doc, page, true, true)

        when:
        def dataTable = new DataTable(table, page)
        dataTable.addListToTable(data, DataTable.NOHEADER)

        then:
        noExceptionThrown()

        cleanup:
        doc?.close()
    }

    def "DataTable handles various data types"() {
        given:
        def doc = new PDDocument()
        def page = new PDPage(PDRectangle.A4)
        doc.addPage(page)

        def data = [
            ["String", "Number", "Boolean"],
            ["text", "123", "true"],
            ["more", "456.78", "false"]
        ]

        def table = new BaseTable(700, 700, 50, 500, 50, doc, page, true, true)

        when:
        def dataTable = new DataTable(table, page)
        dataTable.addListToTable(data, DataTable.HASHEADER)

        then:
        noExceptionThrown()

        cleanup:
        doc?.close()
    }

    def "DataTable can be drawn"() {
        given:
        def doc = new PDDocument()
        def page = new PDPage(PDRectangle.A4)
        doc.addPage(page)

        def data = [
            ["Name", "Value"],
            ["Item 1", "100"],
            ["Item 2", "200"]
        ]

        def table = new BaseTable(700, 700, 50, 500, 50, doc, page, true, true)

        when:
        def dataTable = new DataTable(table, page)
        dataTable.addListToTable(data, DataTable.HASHEADER)
        table.draw()

        then:
        noExceptionThrown()

        cleanup:
        doc?.close()
    }
}
