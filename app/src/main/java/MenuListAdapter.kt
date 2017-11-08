//
//import android.content.Context
//import android.support.design.widget.CoordinatorLayout.Behavior.setTag
//import android.content.Context.LAYOUT_INFLATER_SERVICE
//import android.view.*
//import android.widget.*
//import kotlinx.android.synthetic.main.drawr_list_item.*
//import android.view.LayoutInflater
//import android.widget.TextView
//
//
//
//class MenuListAdapter// Constructor ------------------------------------------------------------
//(
//        // Fields -----------------------------------------------------------------
//        private val mcontext: Context,
//        private val titles: Array<String>,
//        private val subtitles: Array<String>,
//        private val icons: IntArray) : BaseAdapter() {
//    private val inflater = LAYOUT_INFLATER_SERVICE
//
//
//    inner class ViewHolder {
//        var txtTitle: TextView? = null
//        var txtSubtitle: TextView? = null
//        var imgIcon: ImageView? = null
//    }
//
//
//
//    // Accessors --------------------------------------------------------------
//    override fun getCount(): Int {
//        return titles.size
//    }
//
//    override fun getItem(position: Int): Any {
//        return titles[position]
//    }
//
//    override fun getItemId(position: Int): Long {
//        return position.toLong()
//    }
//
//    // Methods ----------------------------------------------------------------
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
//        var convertView = convertView
//
//        val viewHolder: ViewHolder
//
//        // Only inflate the view if convertView is null
//        if (convertView == null) {
//            viewHolder = ViewHolder()
//
//            if (inflater != null) {
//                convertView = inflater.inflate(R.layout.drawer_list_item, parent, false)
//
//                viewHolder.txtTitle = convertView!!.findViewById(
//                        R.id.title)
//                viewHolder.txtSubtitle = convertView!!.findViewById(
//                        R.id.subtitle)
//                viewHolder.imgIcon = convertView!!.findViewById(
//                        R.id.icon) as ImageView
//
//                // This is the first time this view has been inflated,
//                // so store the view holder in its tag fields
//                convertView!!.setTag(viewHolder)
//            } else {
//
//            }
//        } else {
//            viewHolder = convertView!!.getTag()
//        }
//
//        // Set the views fields as needed
//        viewHolder.txtTitle!!.text = titles[position]
//        viewHolder.txtSubtitle!!.text = subtitles[position]
//        viewHolder.imgIcon!!.setImageResource(icons[position])
//
//        return convertView
//    }
//
//    // Classes ----------------------------------------------------------------
//    internal class ViewHolder {
//        var txtTitle: TextView? = null
//        var txtSubtitle: TextView? = null
//        var imgIcon: ImageView? = null
//    }
//
//}