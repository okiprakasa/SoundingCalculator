package bc.okimatra.soundingcalculator

import android.print.PrintDocumentAdapter
import android.print.PrintAttributes
import android.os.Bundle
import android.os.CancellationSignal
import android.print.PrintDocumentInfo
import android.print.PageRange
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.*
import java.lang.Exception

class PdfDocumentAdapter(var path: String) : PrintDocumentAdapter() {
    override fun onLayout(
        oldAttributes: PrintAttributes,
        printAttributes1: PrintAttributes,
        cancellationSignal: CancellationSignal,
        layoutResultCallback: LayoutResultCallback,
        extras: Bundle
    ) {
        if (cancellationSignal.isCanceled) layoutResultCallback.onLayoutCancelled() else {
            val builder = PrintDocumentInfo.Builder("file name")
            builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build()
            layoutResultCallback.onLayoutFinished(
                builder.build(),
                printAttributes1 != printAttributes1
            )
        }
    }

    override fun onWrite(
        pages: Array<PageRange>,
        parcelFileDescriptor: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        writeResultCallback: WriteResultCallback
    ) {
        var `in`: InputStream? = null
        var out: OutputStream? = null
        try {
            val file = File(path)
            `in` = FileInputStream(file)
            out = FileOutputStream(parcelFileDescriptor.fileDescriptor)
            val buff = ByteArray(16384)
            var size: Int
            while (`in`.read(buff).also { size = it } >= 0 && !cancellationSignal.isCanceled) {
                out.write(buff, 0, size)
            }
            if (cancellationSignal.isCanceled) writeResultCallback.onWriteCancelled() else {
                writeResultCallback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            }
        } catch (e: Exception) {
            writeResultCallback.onWriteFailed(e.message)
            Log.e("okimatra", e.message!!)
            e.printStackTrace()
        } finally {
            try {
                `in`!!.close()
                out!!.close()
            } catch (ex: IOException) {
                Log.e("okimatra", "" + ex.message)
            }
        }
    }
}