import Model.Information
import ViewModel.MainViewModel
import android.animation.AnimatorSet
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.sportsapp.R
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.ContextCompat.startActivity

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
//            Toast.makeText(context,"List is empty", Toast.LENGTH_LONG).show()
        }
        else{

        }

        return arrayList.size
    }

    //Bind the items to corresponding view resources and display information
    //set an onCLickListener to animate the expansion of the item.xml card view
    inner class ViewHolder(private val binding: View): RecyclerView.ViewHolder(binding){
        fun bind(info: Information){
            binding.findViewById<TextView>(R.id.title).text = info.title
            binding.findViewById<TextView>(R.id.subTitleOne).text = info.subTitleOne
            binding.findViewById<TextView>(R.id.subTitleTwo).text = info.subTitleTwo
            binding.findViewById<TextView>(R.id.subTitleThree).text = info.subTitleThree
            binding.findViewById<TextView>(R.id.description).text = info.description
            binding.findViewById<ImageView>(R.id.image).setImageBitmap(info.image)

            binding.findViewById<TextView>(R.id.readMoreLink).setOnClickListener{
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(info.teamPageUrl))
                context.startActivity(browserIntent)
            }

            var thisCard: CardView = binding.findViewById<CardView>(R.id.card_item)
            var currentCardHeight = thisCard.layoutParams.height
            var newCardHeight = currentCardHeight * 5

            var expanded = false

            println("CURRENT CARD HEIGHT: $currentCardHeight")

            thisCard.setOnClickListener {
                if (expanded){
                    expandCard(thisCard, newCardHeight, currentCardHeight)
                    expanded = false
                }
                else{
                    expandCard(thisCard, currentCardHeight, newCardHeight)
                    expanded = true
                }
            }
        }

        fun expandCard(cardView: CardView, currentHeight: Int, newHeight: Int){
            val slideAnimator = ValueAnimator
                .ofInt(currentHeight, newHeight)
                .setDuration(250)

            // We use an update listener which listens to each tick
            // and manually updates the height of the view

            slideAnimator.addUpdateListener { animation1: ValueAnimator ->
                val value = animation1.animatedValue as Int
                cardView.layoutParams.height = value
                cardView.requestLayout()
            }

            //We use an animationSet to play the animation
            var animationSet: AnimatorSet = AnimatorSet()

            animationSet.interpolator = AccelerateDecelerateInterpolator()
            animationSet.play(slideAnimator)
            animationSet.start()
        }
    }


}