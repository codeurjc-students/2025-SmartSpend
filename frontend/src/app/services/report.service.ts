import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

export interface ReportData {
  bankAccount: any;
  incomesList: any[];
  expensesList: any[];
  stadistics: {
    incomesTotal: number;
    expensesTotal: number;
    balance: number;
  };
  barLineChart: any;
  expensePieChart: any;
  incomePieChart: any;
  lineChart: any;
}

@Injectable({
  providedIn: 'root'
})
export class ReportService {

  private apiUrl = `${environment.apiUrl}/report`;

  constructor(private http: HttpClient) { }

  /**
   * Obtiene los datos del reporte mensual
   */
  getReportData(bankAccountId: number, year: number, month: number): Observable<ReportData> {
    const params = {
      bankAccountId: bankAccountId.toString(),
      year: year.toString(),
      month: month.toString()
    };
    
    return this.http.get<ReportData>(`${this.apiUrl}/report-data`, { params });
  }

  /**
   * Genera PDF usando jsPDF en el frontend con gráficos
   */
  async generateMonthlyPdf(reportData: ReportData, year: number, month: number): Promise<void> {
    try {
      console.log('=== Iniciando generación de PDF ===');
      console.log('Report Data recibida:', reportData);
      console.log('Stadistics:', JSON.stringify(reportData.stadistics, null, 2));
      
      if (!reportData) {
        throw new Error('reportData es undefined o null');
      }

      if (!reportData.bankAccount) {
        console.warn('⚠️ bankAccount no está definido, usando valores por defecto');
      }

      const doc = new jsPDF();
      const monthNames = [
        'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
        'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
      ];

      let yPosition = 20;
      const lineHeight = 7;
      const pageWidth = doc.internal.pageSize.width;
      const pageHeight = doc.internal.pageSize.height;
      
      console.log('📄 Página 1: Agregando logo...');
      yPosition = await this.addLogo(doc, yPosition);
      
      console.log('📄 Página 1: Agregando título principal...');
      // Título principal con fondo
      doc.setFillColor(30, 41, 59); // Color slate-800
      doc.rect(15, yPosition - 5, pageWidth - 30, 25, 'F');
      
      doc.setTextColor(255, 255, 255); // Texto blanco
      doc.setFontSize(22);
      doc.setFont('helvetica', 'bold');
      doc.text('REPORTE FINANCIERO MENSUAL', pageWidth / 2, yPosition + 8, { align: 'center' });
      
      doc.setFontSize(14);
      doc.setFont('helvetica', 'normal');
      const accountName = reportData.bankAccount?.accountName || 'Cuenta Bancaria';
      doc.text(`${monthNames[month - 1]} ${year} - ${accountName}`, pageWidth / 2, yPosition + 16, { align: 'center' });
      
      yPosition += 35;
      doc.setTextColor(0, 0, 0); // Volver a negro

      // Separador decorativo
      doc.setLineWidth(1);
      doc.setDrawColor(59, 130, 246); // Color azul
      doc.line(20, yPosition, pageWidth - 20, yPosition);
      yPosition += 15;

      console.log('📄 Página 1: Agregando resumen financiero...');
      // Resumen Financiero en caja destacada
      yPosition = this.addFinancialSummaryBox(doc, reportData, yPosition);

      // Separador
      yPosition += 10;
      doc.setLineWidth(0.5);
      doc.setDrawColor(203, 213, 224); // Color gris claro
      doc.line(20, yPosition, pageWidth - 20, yPosition);
      yPosition += 15;

      // Capturar gráficos
      console.log('📊 Capturando gráficos...');
      yPosition = await this.addChartsToPDF(doc, yPosition);
      
      console.log('📄 Página 2: Agregando tablas de transacciones...');
      // Nueva página para las tablas
      doc.addPage();
      yPosition = 30;

      // Título de la página de transacciones
      doc.setFillColor(30, 41, 59);
      doc.rect(15, yPosition - 10, pageWidth - 30, 20, 'F');
      doc.setTextColor(255, 255, 255);
      doc.setFontSize(18);
      doc.setFont('helvetica', 'bold');
      doc.text('DETALLE DE TRANSACCIONES', pageWidth / 2, yPosition, { align: 'center' });
      doc.setTextColor(0, 0, 0);
      yPosition += 25;

      // Tabla de ingresos
      if (reportData.incomesList && reportData.incomesList.length > 0) {
        console.log(`📥 Agregando ${reportData.incomesList.length} ingresos...`);
        yPosition = this.addStyledTransactionTable(doc, 'Ingresos del Mes', reportData.incomesList, yPosition, 'income');
        yPosition += 20;
      } else {
        console.log('⚠️ No hay ingresos para mostrar');
      }

      // Tabla de gastos
      if (reportData.expensesList && reportData.expensesList.length > 0) {
        console.log(`📤 Agregando ${reportData.expensesList.length} gastos...`);
        // Si no hay espacio, nueva página
        if (yPosition > 200) {
          doc.addPage();
          yPosition = 30;
        }
        yPosition = this.addStyledTransactionTable(doc, 'Gastos del Mes', reportData.expensesList, yPosition, 'expense');
      } else {
        console.log('⚠️ No hay gastos para mostrar');
      }

      console.log('📄 Agregando pie de página...');
      // Pie de página estilizado en todas las páginas
      const pageCount = (doc as any).internal.pages.length - 1;
      for (let i = 1; i <= pageCount; i++) {
        doc.setPage(i);
        this.addStyledFooter(doc, pageWidth, pageHeight);
      }

      // Descargar
      const filename = `SmartSpend_Reporte_${monthNames[month - 1]}_${year}.pdf`;
      console.log(`✅ Descargando PDF: ${filename}`);
      doc.save(filename);
      
      console.log('✅ PDF generado y descargado exitosamente!');
      console.log('=== Generación de PDF completada ===');
      
      // Mostrar notificación al usuario
      alert(`✅ PDF generado exitosamente: ${filename}`);

    } catch (error) {
      console.error('❌ Error generando PDF:', error);
      console.error('Error completo:', JSON.stringify(error, null, 2));
      alert(`❌ Error al generar el PDF: ${error instanceof Error ? error.message : String(error)}`);
      throw error; // Relanzar para que el componente lo capture
    }
  }

  private async addChartsToPDF(doc: jsPDF, startY: number): Promise<number> {
    try {
      console.log('Capturando gráficos...');
      let yPosition = startY;
      const chartWidth = 90; // Ancho del gráfico en mm
      const chartHeight = 70; // Alto del gráfico en mm
      const pageWidth = doc.internal.pageSize.width;

      // Esperar más tiempo para que se rendericen los gráficos
      await new Promise(resolve => setTimeout(resolve, 2000));
      
      // Buscar todos los canvas en la página
      const allCanvases = Array.from(document.querySelectorAll('canvas')) as HTMLCanvasElement[];
      console.log(`Encontrados ${allCanvases.length} canvas elements`);

      if (allCanvases.length === 0) {
        console.warn('No se encontraron gráficos para incluir en el PDF');
        doc.setFontSize(11);
        doc.setFont('helvetica', 'italic');
        doc.setTextColor(107, 114, 128);
        doc.text('📊 Gráficos: No disponibles en este momento', 20, yPosition);
        return yPosition + 15;
      }

      let chartCount = 0;
      const chartTitles = ['Gráfico de Ingresos', 'Gráfico de Gastos', 'Gráfico de Línea', 'Gráfico de Barras'];
      const maxCharts = Math.min(4, allCanvases.length);

      for (let i = 0; i < maxCharts; i++) {
        const canvas = allCanvases[i];
        
        if (canvas && canvas.width > 0 && canvas.height > 0) {
          console.log(`Capturando canvas ${chartCount + 1}/${maxCharts}`);
          
          // Verificar si necesitamos nueva página
          if (yPosition > 180) {
            doc.addPage();
            yPosition = 20;
          }

          // Título del gráfico
          doc.setFontSize(12);
          doc.setFont('helvetica', 'bold');
          doc.setTextColor(30, 41, 59);
          const title = chartTitles[chartCount] || `Gráfico ${chartCount + 1}`;
          doc.text(title, 20, yPosition);
          yPosition += 8;

          try {
            // Capturar canvas como imagen PNG
            const imgData = canvas.toDataURL('image/png', 0.9);
            
            if (imgData && imgData.length > 100 && imgData !== 'data:,') { // Verificar que sea válido
              doc.addImage(imgData, 'PNG', (pageWidth - chartWidth) / 2, yPosition, chartWidth, chartHeight);
              console.log(`✅ ${title} agregado exitosamente`);
              yPosition += chartHeight + 12;
            } else {
              console.warn(`⚠️ Canvas ${title} está vacío o inválido`);
              doc.setFontSize(10);
              doc.setFont('helvetica', 'italic');
              doc.setTextColor(107, 114, 128);
              doc.text('(No se pudo capturar el gráfico)', 20, yPosition);
              yPosition += 10;
            }
            chartCount++;
          } catch (imgError) {
            console.warn(`⚠️ Error capturando ${title}:`, imgError);
            doc.setFontSize(10);
            doc.setFont('helvetica', 'italic');
            doc.setTextColor(239, 68, 68);
            doc.text(`(Error al cargar ${title})`, 20, yPosition);
            yPosition += 10;
          }
        }
      }

      if (chartCount === 0) {
        console.warn('No se pudieron capturar gráficos válidos');
        doc.setFontSize(11);
        doc.setFont('helvetica', 'italic');
        doc.setTextColor(107, 114, 128);
        doc.text('📊 Gráficos: No se pudieron capturar correctamente', 20, yPosition);
        yPosition += 15;
      } else {
        console.log(`✅ ${chartCount} gráficos agregados al PDF exitosamente`);
      }

      return yPosition;
    } catch (error) {
      console.error('❌ Error agregando gráficos al PDF:', error);
      doc.setFontSize(11);
      doc.setFont('helvetica', 'italic');
      doc.setTextColor(239, 68, 68);
      doc.text('📊 Gráficos: Error cargando gráficos', 20, startY);
      return startY + 20;
    }
  }



  private async addLogo(doc: jsPDF, startY: number): Promise<number> {
    try {
      // Intenta agregar logo si está disponible en base64
      const logoBase64 = 'data:image/png;base64,TU_LOGO_EN_BASE64_AQUI';
      
      // Si el placeholder no está reemplazado, usa texto alternativo
      if (!logoBase64 || logoBase64.includes('TU_LOGO_EN_BASE64_AQUI')) {
        // Usar texto temporal como fallback
        doc.setFontSize(18);
        doc.setFont('helvetica', 'bold');
        doc.setTextColor(30, 41, 59);
        doc.text('SmartSpend', 20, startY + 8);
        
        doc.setLineWidth(2);
        doc.setDrawColor(59, 130, 246);
        doc.line(20, startY + 12, 80, startY + 12);
        
        return startY + 20;
      }
      
      // Si tenemos un logo válido, agregarlo
      try {
        const logoWidth = 40;  // Ancho en mm
        const logoHeight = 12; // Alto en mm
        doc.addImage(logoBase64, 'PNG', 20, startY, logoWidth, logoHeight);
        return startY + logoHeight + 10;
      } catch (imgError) {
        console.warn('Error agregando imagen de logo, usando texto alternativo:', imgError);
        // Fallback a texto si la imagen falla
        doc.setFontSize(18);
        doc.setFont('helvetica', 'bold');
        doc.setTextColor(30, 41, 59);
        doc.text('SmartSpend', 20, startY + 8);
        
        doc.setLineWidth(2);
        doc.setDrawColor(59, 130, 246);
        doc.line(20, startY + 12, 80, startY + 12);
        
        return startY + 20;
      }
    } catch (error) {
      console.warn('Error agregando logo:', error);
      return startY;
    }
  }

  private addFinancialSummaryBox(doc: jsPDF, reportData: ReportData, startY: number): number {
    const pageWidth = doc.internal.pageSize.width;
    let yPosition = startY;

    // Calcular totales desde las transacciones si no están en estadísticas
    let incomesTotal = 0;
    let expensesTotal = 0;
    
    if (reportData.incomesList && reportData.incomesList.length > 0) {
      incomesTotal = reportData.incomesList.reduce((sum, transaction) => 
        sum + Math.abs(transaction.amount || 0), 0);
    }
    
    if (reportData.expensesList && reportData.expensesList.length > 0) {
      expensesTotal = reportData.expensesList.reduce((sum, transaction) => 
        sum + Math.abs(transaction.amount || 0), 0);
    }
    
    // Usar estadísticas si están disponibles, sino usar cálculos propios
    const finalIncomesTotal = reportData.stadistics?.incomesTotal || incomesTotal;
    const finalExpensesTotal = reportData.stadistics?.expensesTotal || expensesTotal;
    const balance = finalIncomesTotal - finalExpensesTotal;

    // Título de la sección
    doc.setFontSize(16);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(30, 41, 59);
    doc.text('Resumen Financiero', pageWidth / 2, yPosition, { align: 'center' });
    yPosition += 15;

    // Tabla vertical simple
    const tableX = pageWidth / 2 - 40;
    const conceptWidth = 50;
    const amountWidth = 30;
    const rowHeight = 10;

    // Encabezados de tabla
    doc.setLineWidth(0.5);
    doc.setDrawColor(203, 213, 224);
    doc.rect(tableX, yPosition, conceptWidth, rowHeight);
    doc.rect(tableX + conceptWidth, yPosition, amountWidth, rowHeight);
    
    doc.setFontSize(12);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(0, 0, 0);
    doc.text('Concepto', tableX + conceptWidth/2, yPosition + 6, { align: 'center' });
    doc.text('Cantidad', tableX + conceptWidth + amountWidth/2, yPosition + 6, { align: 'center' });
    yPosition += rowHeight;

    // Fila de ingresos
    doc.rect(tableX, yPosition, conceptWidth, rowHeight);
    doc.rect(tableX + conceptWidth, yPosition, amountWidth, rowHeight);
    doc.setFont('helvetica', 'normal');
    doc.text('Ingresos', tableX + 5, yPosition + 6);
    doc.text(`€${finalIncomesTotal.toFixed(2)}`, tableX + conceptWidth + amountWidth/2, yPosition + 6, { align: 'center' });
    yPosition += rowHeight;

    // Fila de gastos
    doc.rect(tableX, yPosition, conceptWidth, rowHeight);
    doc.rect(tableX + conceptWidth, yPosition, amountWidth, rowHeight);
    doc.text('Gastos', tableX + 5, yPosition + 6);
    doc.text(`€${finalExpensesTotal.toFixed(2)}`, tableX + conceptWidth + amountWidth/2, yPosition + 6, { align: 'center' });
    yPosition += rowHeight;

    // Fila de balance con color
    doc.rect(tableX, yPosition, conceptWidth, rowHeight);
    doc.rect(tableX + conceptWidth, yPosition, amountWidth, rowHeight);
    doc.setFont('helvetica', 'bold');
    doc.text('Balance', tableX + 5, yPosition + 6);
    
    // Solo el balance tiene color
    const balanceColor = balance >= 0 ? [34, 197, 94] : [239, 68, 68];
    doc.setTextColor(balanceColor[0], balanceColor[1], balanceColor[2]);
    doc.text(`€${balance.toFixed(2)}`, tableX + conceptWidth + amountWidth/2, yPosition + 6, { align: 'center' });
    
    doc.setTextColor(0, 0, 0); // Volver a negro
    return yPosition + 20;
  }

  private addStyledTransactionTable(doc: jsPDF, title: string, transactions: any[], startY: number, type: 'income' | 'expense'): number {
    try {
      const pageWidth = doc.internal.pageSize.width;
      let yPosition = startY;
      const rowHeight = 8;

      // Título simple sin fondo ni símbolos
      doc.setFontSize(14);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(0, 0, 0);
      doc.text(title, 25, yPosition);
      yPosition += 15;

      // Encabezados de la tabla estilizados
      doc.setFillColor(71, 85, 105); // Gris oscuro
      doc.rect(20, yPosition - 3, pageWidth - 40, 12, 'F');
      
      doc.setTextColor(255, 255, 255);
      doc.setFontSize(11);
      doc.setFont('helvetica', 'bold');
      doc.text('DESCRIPCIÓN', 25, yPosition + 5);
      doc.text('CANTIDAD', 85, yPosition + 5);
      doc.text('FECHA', 125, yPosition + 5);
      doc.text('CATEGORÍA', 160, yPosition + 5);
      yPosition += 12;

      doc.setTextColor(0, 0, 0);

      // Datos de transacciones con filas alternadas
      const transactionsToShow = transactions.slice(0, 20);
      
      transactionsToShow.forEach((transaction, index) => {
        try {
          // Verificar si necesitamos nueva página
          if (yPosition > 270) {
            doc.addPage();
            yPosition = 30;
          }

          // Fila con color alternado
          if (index % 2 === 0) {
            doc.setFillColor(249, 250, 251); // Gris muy claro para filas pares
            doc.rect(20, yPosition - 2, pageWidth - 40, rowHeight, 'F');
          }

          const description = transaction?.description?.length > 16 ? 
            transaction.description.substring(0, 16) + '...' : 
            (transaction?.description || 'N/A');
          
          const amount = transaction?.amount ? Math.abs(transaction.amount).toFixed(2) : '0.00';
          
          // Formatear fecha
          let date = 'N/A';
          const dateField = transaction?.transactionDate || transaction?.date;
          
          if (dateField) {
            try {
              let dateObj;
              
              if (Array.isArray(dateField) && dateField.length >= 3) {
                const [year, month, day] = dateField;
                dateObj = new Date(year, month - 1, day);
              } else if (typeof dateField === 'string') {
                dateObj = new Date(dateField);
              }
              
              if (dateObj && !isNaN(dateObj.getTime())) {
                date = dateObj.toLocaleDateString('es-ES', {
                  day: '2-digit',
                  month: '2-digit',
                  year: '2-digit'
                });
              }
            } catch (dateError) {
              date = 'N/A';
            }
          }
          
          const categoryName = transaction?.category?.name?.length > 10 ? 
            transaction.category.name.substring(0, 10) + '...' : 
            (transaction?.category?.name || 'N/A');

          // Texto de las celdas
          doc.setFontSize(10);
          doc.setFont('helvetica', 'normal');
          doc.setTextColor(0, 0, 0);
          doc.text(description, 25, yPosition + 4);
          doc.setFont('helvetica', 'bold');
          doc.text(`€${amount}`, 85, yPosition + 4);
          doc.setFont('helvetica', 'normal');
          doc.text(date, 125, yPosition + 4);
          doc.text(categoryName, 160, yPosition + 4);
          
          yPosition += rowHeight;
          
        } catch (transactionError) {
          console.error(`Error procesando transacción ${index + 1}:`, transactionError);
        }
      });

      // Línea de total
      if (transactionsToShow.length > 0) {
        doc.setLineWidth(1);
        doc.setDrawColor(203, 213, 224);
        doc.line(20, yPosition, pageWidth - 20, yPosition);
        yPosition += 5;

        const total = transactionsToShow.reduce((sum, t) => sum + Math.abs(t.amount || 0), 0);
        doc.setFont('helvetica', 'bold');
        doc.setTextColor(0, 0, 0);
        doc.text(`TOTAL: €${total.toFixed(2)}`, pageWidth - 25, yPosition + 5, { align: 'right' });
        yPosition += 15;
      }

      if (transactions.length > 20) {
        doc.setFontSize(9);
        doc.setFont('helvetica', 'italic');
        doc.setTextColor(107, 114, 128);
        doc.text(`... y ${transactions.length - 20} transacciones más`, 25, yPosition);
        yPosition += 10;
      }
      
      return yPosition;
      
    } catch (error) {
      console.error(`Error generando tabla ${title}:`, error);
      return startY + 25;
    }
  }

  private addStyledFooter(doc: jsPDF, pageWidth: number, pageHeight: number): void {
    // Línea decorativa
    doc.setLineWidth(2);
    doc.setDrawColor(59, 130, 246);
    doc.line(20, pageHeight - 35, pageWidth - 20, pageHeight - 35);

    // Información de SmartSpend
    doc.setFontSize(10);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(30, 41, 59);
    doc.text('SmartSpend - Gestión Financiera Inteligente', pageWidth / 2, pageHeight - 25, { align: 'center' });

    // Fecha de generación
    const currentDate = new Date().toLocaleDateString('es-ES', {
      day: 'numeric',
      year: 'numeric',
      month: 'long'
    });
    doc.setFontSize(8);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(107, 114, 128);
    doc.text(`Reporte generado el ${currentDate}`, pageWidth / 2, pageHeight - 15, { align: 'center' });
  }
}