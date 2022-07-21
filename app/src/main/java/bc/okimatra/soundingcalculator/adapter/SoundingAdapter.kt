package bc.okimatra.soundingcalculator.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import bc.okimatra.soundingcalculator.R
import bc.okimatra.soundingcalculator.databinding.ItemsRowSoundingBinding
import bc.okimatra.soundingcalculator.datasetup.SoundingEntity
import java.text.DecimalFormat

class SoundingAdapter(private val items: ArrayList<SoundingEntity>,
                      private val updateListener:(id:Int)->Unit,
                      private val deleteListener:(id:Int)->Unit,
                      private val pdfListener:(id:Int)->Unit):
    RecyclerView.Adapter<SoundingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemsRowSoundingBinding.inflate(
            LayoutInflater.from(parent.context),parent,false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val item = items[position]
        val hariTanggal = item.waktu.replace("-"," ").subSequence(0, item.waktu.indexOf(":")-3).toString()
        val pukul = item.waktu.subSequence(item.waktu.indexOf(":")-2, item.waktu.length).toString()
        val nondec = DecimalFormat("#,###")

        if (item.no_tangki.length<9) {
            holder.tvNoTangki.text = String.format(context.getString(R.string.no_tangki_edited,item.no_tangki))
        } else {
            holder.tvNoTangki.text = String.format(context.getString(R.string.no_tangki_edited,"${item.no_tangki.subSequence(0,7)}..."))
        }
        holder.tvHasil.text = String.format(context.getString(R.string.hasil_akhir_edited_kgm, (nondec.format((item.hasil_sounding*1000.0).toBigDecimal())).replace(",",".")))
        holder.tvWaktu.text = hariTanggal
        holder.tvJamSounding.text = String.format(context.getString(R.string.waktu_edited, pukul))
        holder.tvNama.text = item.perusahaan_sounding

        holder.llMain.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))

        holder.ivEdit.setOnClickListener {
         updateListener.invoke(item.id)
        }
        holder.iconBackground.setOnClickListener {
         updateListener.invoke(item.id)
        }

        holder.ivDelete.setOnClickListener {
          deleteListener.invoke(item.id)
        }

        holder.background.setOnClickListener {
            pdfListener.invoke(item.id)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(binding: ItemsRowSoundingBinding) : RecyclerView.ViewHolder(binding.root) {
        val llMain = binding.llMain
        val tvNama = binding.tvNamaPerusahaan
        val tvNoTangki = binding.tvNoTangki
        val tvWaktu = binding.tvWaktu
        val tvJamSounding = binding.tvJamSounding
        val tvHasil = binding.tvHasil
        val ivEdit = binding.ivEdit
        val ivDelete = binding.ivDelete
        val background = binding.background
        val iconBackground = binding.layoutIcon
    }
}