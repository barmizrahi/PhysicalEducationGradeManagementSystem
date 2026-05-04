import apiClient from './client'
import { ExportConfig } from '../types'

/**
 * Export API service
 */
export const exportApi = {
  /**
   * Export grades to Excel file
   * Returns a blob that can be downloaded
   */
  exportGrades: async (config: ExportConfig): Promise<Blob> => {
    const response = await apiClient.post('/export/excel', config, {
      responseType: 'blob',
    })
    return response.data
  },

  /**
   * Helper function to trigger download of exported file
   */
  downloadExportedFile: (blob: Blob, filename: string = 'grades.xlsx'): void => {
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  },
}
