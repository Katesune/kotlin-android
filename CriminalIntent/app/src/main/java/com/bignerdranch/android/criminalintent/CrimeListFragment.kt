package com.bignerdranch.android.criminalintent

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.util.*

private const val TAG = "CrimeListFragment"

class CrimeListFragment: Fragment() {

    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private var callbacks: Callbacks? = null
    private lateinit var crimes: List<Crime>
    private lateinit var crimeRecyclerView: RecyclerView
    private lateinit var emptyListTextView: TextView
    private lateinit var addCrimeButton: Button
    private var adapter: CrimeAdapter? = CrimeAdapter()

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        crimes = emptyList()
        crimeListViewModel.loadCrimes(crimes)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)

        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView
        emptyListTextView = view.findViewById(R.id.empty_list) as TextView
        addCrimeButton = view.findViewById(R.id.add_crime) as Button

        crimeRecyclerView.layoutManager = LinearLayoutManager(context)

        crimeRecyclerView.adapter = adapter
        crimeRecyclerView.itemAnimator = null

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimeListLiveData.observe(
            viewLifecycleOwner,
            Observer { crimes ->
                crimes?.let {
                    Log.i(TAG, "Got crimes ${crimes.size}")

                    crimes.isEmpty().apply {
                        emptyListTextView.isVisible = this
                        addCrimeButton.isVisible = this
                    }

                    this.crimes = crimes
                    adapter?.submitList(crimes.toMutableList())
                }
            }
        )

        addCrimeButton.setOnClickListener {
            addCrime()
        }
    }

    private fun addCrime() {
        val crime = Crime()
        crimeListViewModel.addCrime(crime)
        callbacks?.onCrimeSelected(crime.id)
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_crime -> {
                addCrime()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private inner class CrimeHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var crime: Crime

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            //dateTextView.text = this.crime.date.toString()
            dateTextView.text = dateConverted()
            solvedImageView.visibility = if (crime.isSolved) {
                View.VISIBLE
            } else View.GONE
        }

        val dateConverted: () -> String = { ->
            DateFormat.format("EEEE, MMMM dd, yyyy", this.crime.date).toString()
        }

        override fun onClick(v: View) {
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    private inner class CrimeAdapter()
        : ListAdapter<Crime, CrimeHolder>(CrimeCallback()) {

        override fun getItemViewType(position: Int): Int {
            return R.layout.list_item_crime
            //return crimes[position].requiredPolice
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
        : CrimeHolder {
            val view = layoutInflater.inflate(viewType, parent, false)
            return CrimeHolder(view)
        }

        override fun getItemCount() = crimes.size

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            view?.addRowDescription(crime)
            holder.bind(crime)
        }
    }

    fun View.addRowDescription(crime: Crime) {
        val titleDescription = getString(R.string.crime_list_title_description) + crime.title
        val dateDescription = getString(R.string.crime_date) + dateDescription(crime.date)
        val solvedDescription =
            if (crime.isSolved) getString(R.string.crime_report_solved)
            else getString(R.string.crime_report_unsolved)

        this.contentDescription = titleDescription + dateDescription + solvedDescription
    }

    val dateDescription: (Date) -> String = { date ->
        DateFormat.format("EEEE, MMMM dd, yyyy HH mm", date).toString()
    }

    private inner class CrimeCallback
        : DiffUtil.ItemCallback<Crime>() {

        override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean =
            oldItem == newItem
    }

    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }
}