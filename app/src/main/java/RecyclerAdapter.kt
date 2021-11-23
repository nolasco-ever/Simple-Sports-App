import Model.Information
import ViewModel.MainViewModel
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.sportsapp.R

class RecyclerAdapter(val viewModel: MainViewModel, val arrayList: ArrayList<Information>, val context: Context): RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        var root = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return ViewHolder(root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(arrayList.get(position))
    }

    override fun getItemCount(): Int {
        if (arrayList.size == 0){
            Toast.makeText(context,"List is empty", Toast.LENGTH_LONG).show()
        }
        else{

        }

        return arrayList.size
    }

    inner class ViewHolder(private val binding: View): RecyclerView.ViewHolder(binding){
        fun bind(info: Information){
            binding.findViewById<TextView>(R.id.title).text = info.title
            binding.findViewById<TextView>(R.id.subTitleOne).text = info.subTitleOne
            binding.findViewById<TextView>(R.id.subTitleTwo).text = info.subTitleTwo
            binding.findViewById<TextView>(R.id.subTitleThree).text = info.subTitleThree
            binding.findViewById<ImageView>(R.id.image).setImageBitmap(info.image)
        }
    }
}