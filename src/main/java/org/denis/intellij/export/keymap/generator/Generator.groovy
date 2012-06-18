package org.denis.intellij.export.keymap.generator

import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import org.denis.intellij.export.keymap.Bundle
import org.jetbrains.annotations.NotNull
import com.itextpdf.text.*

import static org.denis.intellij.export.keymap.generator.GenerationConstants.*
import org.denis.intellij.export.keymap.model.DataEntry
import org.denis.intellij.export.keymap.model.DataVisitor
import org.denis.intellij.export.keymap.model.ActionData
import org.denis.intellij.export.keymap.model.Header

import org.denis.intellij.export.keymap.model.ColumnBreak
import org.denis.intellij.export.keymap.model.ActionsProfile

/**
 * @author Denis Zhdanov
 * @since 6/13/12 6:10 PM
 */
class Generator {

  // TODO den remove
  public static void main(String[] args) {
    new Generator().generate(ActionsProfile.DEFAULT.entries, "/home/denis/Downloads/output.pdf", "Default")
  }
  
  def generate(@NotNull java.util.List<DataEntry> data, @NotNull String outputPath, String keymapName) {
    def context = new GenerationContext(outputPath: outputPath, data: data, keymapName: keymapName)
    def table = doGenerate(context)
    context.prepareToRealGeneration(table)
    doGenerate(context)
  }

  private def doGenerate(@NotNull GenerationContext context) {
    def document = new Document()
    context.document = document
    document.setPageSize(PageSize.A4.rotate())
    PdfWriter.getInstance(document, new BufferedOutputStream(new FileOutputStream(context.outputPath)))
    document.addAuthor(Bundle.message("document.author"))
    document.addSubject(Bundle.message("document.subject", context.keymapName))
    document.open()

    PdfPTable rootTable = new PdfPTable(COLUMNS_NUMBER)
    rootTable.setWidthPercentage(100)
    COLUMNS_NUMBER.times { rootTable.addCell(new PdfPCell(paddingLeft: 0f, paddingRight: 20f, border: Rectangle.NO_BORDER)) }
    rootTable.getRow(0).getCells().each { context.columns << it }

    addData(context)

    document.add(rootTable)
    document.close()
    rootTable
  }

  private def addData(@NotNull GenerationContext context) {
    java.util.List<Header> activeHeaders = []
    def visitor = new DataVisitor() {
      @Override
      void visit(ActionData data) {
        if (activeHeaders) {
          addHeaders(activeHeaders, context)
          activeHeaders.clear()
        }
        
        def padding = 2f
        
        def keyFont = new Font(FONT_FAMILY, DATA_FONT_SIZE, Font.BOLD)
        def keyCell = new PdfPCell(new Paragraph(data.shortcut, keyFont))
        keyCell.border = Rectangle.BOTTOM
        keyCell.backgroundColor = COLOR_BACKGROUND_KEY
        keyCell.borderColor = COLOR_BORDER_DATA
        keyCell.padding = padding
        context.dataTable.addCell(keyCell)

        def valueFont = new Font(FONT_FAMILY, DATA_FONT_SIZE)
        def valueCell = new PdfPCell(new Paragraph(data.description, valueFont))
        valueCell.border = Rectangle.BOTTOM
        valueCell.borderColor = COLOR_BORDER_DATA
        valueCell.padding = padding
        context.dataTable.addCell(valueCell)
        context.onNewRow()
      }

      @Override void visit(Header data) { activeHeaders << data }

      @Override
      void visit(ColumnBreak columnBreak) {
        if (context.realGenerationIteration) {
          context.nextColumn()
        }
      }
    }
    
    context.data.each { visitor.visit(it) }
  }
  
  private static void addHeaders(@NotNull java.util.List<Header> headers, @NotNull GenerationContext context) {
    context.updateColumn(headers.size() + 1 /* at least one data row */)
    context.newTable()
    def font = new Font(FONT_FAMILY, ACTION_GROUP_FONT_SIZE, Font.BOLD, TITLE_COLOR)
    def addCell = { String title, boolean withBorder ->
      def titleCell = new PdfPCell(new Paragraph(title, font))
      titleCell.colspan = 2
      if (withBorder) {
        titleCell.border = Rectangle.BOTTOM
        titleCell.borderColor = COLOR_BORDER_HEADER
      }
      else {
        titleCell.border = Rectangle.NO_BORDER
      }
      
      context.dataTable.addCell(titleCell)
    }
    for (header in headers.subList(0, headers.size() - 1)) {
      addCell(header.name, false)
    }
    addCell(headers.last().name, true)
    headers.size().times { context.onNewRow() }
  }
}