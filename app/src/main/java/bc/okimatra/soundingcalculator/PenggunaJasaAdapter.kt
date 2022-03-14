package bc.okimatra.soundingcalculator

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import bc.okimatra.soundingcalculator.databinding.ItemsRowCompanyBinding
import bc.okimatra.soundingcalculator.datasetup.PenggunaJasaEntity

class PenggunaJasaAdapter(private val items: ArrayList<PenggunaJasaEntity>,
                          private val updateListener:(id:Int)->Unit,
                          private val deleteListener:(id:Int)->Unit) :
    RecyclerView.Adapter<PenggunaJasaAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemsRowCompanyBinding.inflate(
            LayoutInflater.from(parent.context),parent,false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val item = items[position]

        holder.tvName.text = item.nama_pengguna_jasa
        holder.tvJabatan.text = item.jabatan
        holder.tvPerusahaan.text = item.perusahaan_pengguna_jasa

        holder.llMain.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))

        holder.ivEdit.setOnClickListener {
         updateListener.invoke(item.id)
        }

        holder.ivDelete.setOnClickListener {
          deleteListener.invoke(item.id)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(binding: ItemsRowCompanyBinding) : RecyclerView.ViewHolder(binding.root) {
        val llMain = binding.llMain
        val tvName = binding.tvName
        val tvJabatan = binding.barisKedua
        val tvPerusahaan = binding.barisKetiga
        val ivEdit = binding.ivEdit
        val ivDelete = binding.ivDelete
    }
}