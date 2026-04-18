package CreditoBD;

import org.apache.poi.xwpf.usermodel.*;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Credito {

    private static final DecimalFormat fmt = new DecimalFormat("$#,###.00");

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String prestamista = "Ma. Guadalupe Romero Contreras";
        String cliente = "Ronaldo Isaias Murillo Aguilar";

        System.out.println("======= SISTEMA DE GESTIÓN DE CRÉDITO =======");
        System.out.print("1. Monto del préstamo: ");
        double monto = sc.nextDouble();
        System.out.print("2. Tasa anual (ej. 12): ");
        double tasaAnual = sc.nextDouble() / 100;
        
        System.out.print("3. Plazo en MESES: ");
        int plazoMeses = sc.nextInt();
        
        System.out.print("4. Mes de inicio (1-12): ");
        int mesIn = sc.nextInt();
        System.out.print("5. ¿Lleva IVA en intereses? (s/n): ");
        boolean conIVA = sc.next().equalsIgnoreCase("s");

        generarDocumentoPro(monto, tasaAnual, plazoMeses, mesIn, conIVA, prestamista, cliente, sc);
        sc.close();
    }

    public static void generarDocumentoPro(double capIni, double tasaAnual, int totalMeses, int mesIn, 
                                          boolean conIVA, String pres, String clie, Scanner sc) {
        try (XWPFDocument doc = new XWPFDocument()) {
            
            XWPFParagraph headerPara = doc.createParagraph();
            headerPara.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun hr = headerPara.createRun();
            hr.setText("Fecha de emisión: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")));
            hr.setItalic(true);

            XWPFParagraph title = doc.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun tr = title.createRun();
            tr.setBold(true);
            tr.setFontSize(20);
            tr.setColor("FF0000");
            tr.setText("ESTADO DE CUENTA Y CONTROL DE PAGOS");
            tr.addBreak();

            XWPFTable infoTable = doc.createTable(2, 2);
            infoTable.removeBorders();
            infoTable.setWidth("100%");
            pintarCeldaInfo(infoTable.getRow(0).getCell(0), "PRESTADOR:", pres);
            pintarCeldaInfo(infoTable.getRow(0).getCell(1), "CLIENTE:", clie);
            pintarCeldaInfo(infoTable.getRow(1).getCell(0), "MONTO TOTAL:", fmt.format(capIni));
            pintarCeldaInfo(infoTable.getRow(1).getCell(1), "TASA Y PLAZO:", (tasaAnual*100) + "% a " + totalMeses + " meses");

            doc.createParagraph().createRun().addBreak();

            XWPFTable table = doc.createTable();
            table.setWidth("100%");
            String[] headers = {"No.", "Fecha", "Capital", "Interés", "IVA", "Total", "Saldo"};
            XWPFTableRow hRow = table.getRow(0);
            for(int i=0; i<headers.length; i++) {
                XWPFTableCell cell = (i==0) ? hRow.getCell(0) : hRow.addNewTableCell();
                cell.setColor("000000");
                XWPFParagraph p = cell.getParagraphs().get(0);
                p.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun r = p.createRun();
                r.setBold(true); r.setColor("FFFFFF"); r.setText(headers[i]);
            }

         double saldo = capIni;
            double sumaCap = 0, sumaInt = 0, sumaIVA = 0, sumaTotal = 0;
            double ahorroInteresTotal = 0;
            LocalDate fecha = LocalDate.now().withMonth(mesIn);
            boolean modoProy = false;
            int mes = 1;

            double cuotaCapBase = capIni / totalMeses;

            while (saldo > 0.01) {
                double pagoTotalIngresado;
                double abonoCapital;
                double intMes = saldo * (tasaAnual / 12);
                double ivaMes = conIVA ? intMes * 0.16 : 0;
                double cargosFijos = intMes + ivaMes;

                if (!modoProy) {
                    System.out.print("¿Cuánto dinero vas a pagar en TOTAL el mes " + mes + "? (0 para proyectar): ");
                    pagoTotalIngresado = sc.nextDouble();

                    if (pagoTotalIngresado <= 0) {
                        modoProy = true;
                        abonoCapital = saldo / (totalMeses - mes + 1);
                        pagoTotalIngresado = abonoCapital + cargosFijos;
                    } else {
                        abonoCapital = pagoTotalIngresado - cargosFijos;
                        if (abonoCapital > cuotaCapBase) {
                            int adelantados = (int)(abonoCapital / cuotaCapBase);
                            System.out.println("   -> Adelantaste capital equivalente a aprox. " + adelantados + " meses.");
                        }
                    }
                } else {
                    abonoCapital = saldo / (totalMeses - mes + 1);
                    pagoTotalIngresado = abonoCapital + cargosFijos;
                }

                if (abonoCapital > saldo) {
                    abonoCapital = saldo;
                    pagoTotalIngresado = abonoCapital + cargosFijos;
                }

                double intNormal = Math.max(0, (capIni - (cuotaCapBase * (mes - 1))) * (tasaAnual / 12));
                if (intNormal > intMes) ahorroInteresTotal += (intNormal - intMes);

                saldo -= abonoCapital;

                XWPFTableRow row = table.createRow();
                row.getCell(0).setText(String.valueOf(mes));
                row.getCell(1).setText(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                row.getCell(2).setText(fmt.format(abonoCapital));
                row.getCell(3).setText(fmt.format(intMes));
                row.getCell(4).setText(fmt.format(ivaMes));
                row.getCell(5).setText(fmt.format(pagoTotalIngresado));
                
                XWPFRun resRun = row.getCell(6).getParagraphs().get(0).createRun();
                resRun.setText(fmt.format(Math.max(0, saldo)));
                if (saldo < 1) resRun.setColor("FF0000");

                if (modoProy) {
                    for(XWPFTableCell c : row.getTableCells()) c.setColor("F4F4F4");
                }

                sumaCap += abonoCapital;
                sumaInt += intMes; 
                sumaIVA += ivaMes; 
                sumaTotal += pagoTotalIngresado;
                
                mes++; fecha = fecha.plusMonths(1);
                if (mes > 600) break; 
            }

            // --- ESTA ES LA FILA DE TOTALES AL FINAL DE LA TABLA ---
            XWPFTableRow totalRow = table.createRow();
            totalRow.getCell(0).setText(""); // No.
            totalRow.getCell(1).setText("TOTALES:");
            totalRow.getCell(2).setText(fmt.format(sumaCap));
            totalRow.getCell(3).setText(fmt.format(sumaInt));
            totalRow.getCell(4).setText(fmt.format(sumaIVA));
            totalRow.getCell(5).setText(fmt.format(sumaTotal));
            totalRow.getCell(6).setText(""); // Saldo final

            // Ponemos la fila de totales en negrita para que resalte
            for (int i = 0; i < 7; i++) {
                XWPFTableCell cell = totalRow.getCell(i);
                cell.setColor("E2E2E2"); // Gris claro para distinguir
                XWPFParagraph p = cell.getParagraphs().get(0);
                if (!p.getRuns().isEmpty()) {
                    p.getRuns().get(0).setBold(true);
                }
            }

            doc.createParagraph().createRun().addBreak();
            XWPFTable fTable = doc.createTable(4, 2);
            fTable.removeBorders();
            fTable.setWidth("100%");
            pintarFilaTotal(fTable.getRow(0), "TOTAL UTILIDAD GENERADA:", fmt.format(sumaInt), "000000");
            pintarFilaTotal(fTable.getRow(1), "TOTAL IVA:", fmt.format(sumaIVA), "000000");
            pintarFilaTotal(fTable.getRow(2), "TOTAL GENERAL PAGADO:", fmt.format(sumaTotal), "FF0000");
            pintarFilaTotal(fTable.getRow(3), "AHORRO POR PAGOS ADELANTADOS:", fmt.format(ahorroInteresTotal), "00B050");

            doc.createParagraph().createRun().addBreak(BreakType.PAGE);
            XWPFParagraph signTitle = doc.createParagraph();
            signTitle.setAlignment(ParagraphAlignment.CENTER);
            signTitle.createRun().setText("CONFORMIDAD DE LAS PARTES");
            
            XWPFTable signTable = doc.createTable(1, 2);
            signTable.removeBorders();
            signTable.setWidth("100%");
            crearLineaFirma(signTable.getRow(0).getCell(0), pres, "EL PRESTADOR");
            crearLineaFirma(signTable.getRow(0).getCell(1), clie, "EL CLIENTE");

            String ruta = System.getProperty("user.home") + "/Downloads/EstadoCuenta_Real.docx";
            try (FileOutputStream out = new FileOutputStream(ruta)) {
                doc.write(out);
                System.out.println("\nDocumento generado exitosamente con fila de totales.");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void pintarCeldaInfo(XWPFTableCell cell, String tag, String val) {
        XWPFParagraph p = cell.getParagraphs().get(0);
        XWPFRun r1 = p.createRun(); r1.setBold(true); r1.setText(tag + " ");
        XWPFRun r2 = p.createRun(); r2.setText(val);
    }

    private static void pintarFilaTotal(XWPFTableRow row, String txt, String val, String color) {
        XWPFParagraph p1 = row.getCell(0).getParagraphs().get(0);
        XWPFRun r1 = p1.createRun(); r1.setBold(true); r1.setFontSize(13); r1.setText(txt);
        XWPFParagraph p2 = row.getCell(1).getParagraphs().get(0);
        p2.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun r2 = p2.createRun(); r2.setBold(true); r2.setFontSize(13); r2.setColor(color); r2.setText(val);
    }

    private static void crearLineaFirma(XWPFTableCell cell, String nombre, String rol) {
        XWPFParagraph p = cell.getParagraphs().get(0);
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r = p.createRun();
        r.addBreak(); r.setText("__________________________");
        r.addBreak(); r.setBold(true); r.setText(nombre);
        r.addBreak(); r.setItalic(true); r.setText(rol);
    }
}