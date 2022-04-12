package bc.okimatra.soundingcalculator.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import bc.okimatra.soundingcalculator.R
import bc.okimatra.soundingcalculator.databinding.ItemsRowBinding
import bc.okimatra.soundingcalculator.datasetup.PegawaiEntity
import bc.okimatra.soundingcalculator.nipSpace

class PegawaiAdapter(private val items: ArrayList<PegawaiEntity>,
                     private val updateListener:(id:Int)->Unit,
                     private val deleteListener:(id:Int)->Unit) :
    RecyclerView.Adapter<PegawaiAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemsRowBinding.inflate(
            LayoutInflater.from(parent.context),parent,false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val item = items[position]

        holder.tvName.text = item.nama_pegawai
        holder.tvBarisKedua.text = nipSpace(item.nip)
        holder.tvBarisKetiga.text = item.kantor_pegawai.replace(("KANTOR PENGAWASAN DAN PELAYANAN BEA DAN CUKAI"),"KPPBC").replace("TIPE MADYA PABEAN","TMP")

        holder.llMain.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))

        holder.ivEdit.setOnClickListener {
         updateListener.invoke(item.id)
        }

        holder.llMain.setOnClickListener {
            updateListener.invoke(item.id)
        }

        holder.ivDelete.setOnClickListener {
          deleteListener.invoke(item.id)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(binding: ItemsRowBinding) : RecyclerView.ViewHolder(binding.root) {
        val llMain = binding.llMain
        val tvName = binding.tvName
        val tvBarisKedua = binding.barisKedua
        val tvBarisKetiga = binding.barisKetiga
        val ivEdit = binding.ivEdit
        val ivDelete = binding.ivDelete
    }
}