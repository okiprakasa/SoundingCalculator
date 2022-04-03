package bc.okimatra.soundingcalculator.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import bc.okimatra.soundingcalculator.R
import bc.okimatra.soundingcalculator.databinding.ItemsRowReportBinding
import bc.okimatra.soundingcalculator.datasetup.ReportEntity
import bc.okimatra.soundingcalculator.endSpaceRemover

class ReportAdapter(private val items: ArrayList<ReportEntity>,
                    private val deleteListener:(id:Int)->Unit,
                    private val pdfListener:(id:Int)->Unit):
    RecyclerView.Adapter<ReportAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemsRowReportBinding.inflate(
            LayoutInflater.from(parent.context),parent,false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val item = items[position]
        val noTangkiUnik = item.no_tangki.distinctBy { it.uppercase() }
        var noSemuaTangki = String()
        for (i in noTangkiUnik.indices) {
            if (i!=0) {
                noSemuaTangki += ", ${noTangkiUnik[i].uppercase()}"
            }
            else {
                noSemuaTangki = noTangkiUnik[i]
            }
        }
        noSemuaTangki = endSpaceRemover(noSemuaTangki)
        if (noSemuaTangki.subSequence(noSemuaTangki.length-1,noSemuaTangki.length) == ",") {
            noSemuaTangki = noSemuaTangki.subSequence(0, noSemuaTangki.length-1).toString()
        }

        if (item.nomor_ba.length < 30) {
            holder.tvNoBa.text = item.nomor_ba
        } else {
            holder.tvNoBa.text = String.format(context.getString(R.string.no_ba_holder),item.nomor_ba.subSequence(0, 18),item.nomor_ba.subSequence(item.nomor_ba.length-8, item.nomor_ba.length))
        }
        holder.tvWaktu.text = item.tanggal_ba.replace("-"," ")
        holder.tvHasil.text = item.hasil_perhitungan.replace(".",",")
        holder.tvBentuk.text = String.format(context.getString(R.string.deskripsi, item.produk, item.bentuk))
        holder.tvPerusahaan.text = String.format(context.getString(R.string.deskripsi, String.format(context.getString(R.string.no_tangki_edited, noSemuaTangki)), item.perusahaan_sounding[0]))

        holder.llMain.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))

        holder.ivDelete.setOnClickListener {
          deleteListener.invoke(item.id)
        }

        holder.ivDown.setOnClickListener {
            holder.tvBentuk.visibility = View.VISIBLE
            holder.ivDown.visibility = View.GONE
            holder.ivUp.visibility = View.VISIBLE
        }

        holder.ivUp.setOnClickListener {
            holder.tvBentuk.visibility = View.GONE
            holder.ivDown.visibility = View.VISIBLE
            holder.ivUp.visibility = View.GONE
        }

        holder.background.setOnClickListener {
            pdfListener.invoke(item.id)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(binding: ItemsRowReportBinding) : RecyclerView.ViewHolder(binding.root) {
        val llMain = binding.llMain
        val tvPerusahaan = binding.tvPerusahaan
        val tvNoBa = binding.tvNoBa
        val tvWaktu = binding.tvWaktu
        val tvHasil = binding.tvHasilSounding
        val tvBentuk = binding.tvBentuk
        val ivDelete = binding.ivDelete
        val ivUp = binding.ivUp
        val ivDown = binding.ivDown
        val background = binding.background
    }
}