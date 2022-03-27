package bc.okimatra.soundingcalculator.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import bc.okimatra.soundingcalculator.R
import bc.okimatra.soundingcalculator.databinding.ItemsRowSoundingBinding
import bc.okimatra.soundingcalculator.datasetup.SoundingEntity

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

        holder.tvNama.text = item.perusahaan_sounding
        holder.tvNoTangki.text = String.format(context.getString(R.string.no_tangki_edited,item.no_tangki))
        holder.tvWaktu.text = item.waktu.replace("-"," ")
        holder.tvHasil.text = String.format(context.getString(R.string.hasil_akhir_edited,item.hasil_sounding.toString()))

        holder.llMain.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))

        holder.ivEdit.setOnClickListener {
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
        val tvHasil = binding.tvHasilSounding
        val ivEdit = binding.ivEdit
        val ivDelete = binding.ivDelete
        val background = binding.background
    }
}