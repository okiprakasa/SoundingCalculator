package bc.okimatra.soundingcalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import bc.okimatra.soundingcalculator.databinding.ItemsRowSoundingBinding
import bc.okimatra.soundingcalculator.datasetup.SoundingEntity

/**
* We have the @param [items] to represent the list that populates the adapter
 * The @param [updateListener] to listen to the edit icon an get the positions id
 * The @param [deleteListener] to listen to the delete icon and get the positions id
**/
class SoundingAdapter(private val items: ArrayList<SoundingEntity>,
                      private val updateListener:(id:Int)->Unit,
                      private val deleteListener:(id:Int)->Unit) :
    RecyclerView.Adapter<SoundingAdapter.ViewHolder>() {

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemsRowSoundingBinding.inflate(
            LayoutInflater.from(parent.context),parent,false)
        )
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val item = items[position]

        holder.tvNama.text = item.perusahaan_sounding
        holder.tvNoTangki.text = String.format(context.getString(R.string.no_tangki_edited,item.noTangki))
        holder.tvWaktu.text = item.waktu.replace("-"," ")
        holder.tvBentuk.text = item.bentuk
        holder.tvHasil.text = String.format(context.getString(R.string.hasil_akhir_edited,item.hasilSounding.toString()))

        holder.llMain.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))

        holder.ivEdit.setOnClickListener {
         updateListener.invoke(item.id)
        }

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
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class ViewHolder(binding: ItemsRowSoundingBinding) : RecyclerView.ViewHolder(binding.root) {
        // Holds the TextView that will add each item to
        val llMain = binding.llMain
        val tvNama = binding.tvNamaPerusahaan
        val tvNoTangki = binding.tvNoTangki
        val tvWaktu = binding.tvWaktu
        val tvHasil = binding.tvHasilSounding
        val tvBentuk = binding.tvBentuk
        val ivEdit = binding.ivEdit
        val ivDelete = binding.ivDelete
        val ivUp = binding.ivUp
        val ivDown = binding.ivDown
    }
}