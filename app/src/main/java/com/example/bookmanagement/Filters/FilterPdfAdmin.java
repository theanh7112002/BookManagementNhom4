package com.example.bookmanagement.Filters;

import android.widget.Filter;

import com.example.bookmanagement.Models.ModelCategory;
import com.example.bookmanagement.Models.ModelPdf;
import com.example.bookmanagement.adapters.AdapterCategory;
import com.example.bookmanagement.adapters.AdapterPdfAdmin;

import java.util.ArrayList;

public class FilterPdfAdmin extends Filter {
    ArrayList<ModelPdf> filterList;
    AdapterPdfAdmin adapterPdfAdmin;

    public FilterPdfAdmin(ArrayList<ModelPdf> filterList, AdapterPdfAdmin adapterPdfAdmin) {
        this.filterList = filterList;
        this.adapterPdfAdmin = adapterPdfAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        if(charSequence !=null && charSequence.length()>0){
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<ModelPdf> filteredModels = new ArrayList<>();
            for(int i=0; i<filterList.size(); i++){
                if(filterList.get(i).getTitle().toUpperCase().contains(charSequence)){
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else{
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        adapterPdfAdmin.pdfArrayList = (ArrayList<ModelPdf>)filterResults.values;

        adapterPdfAdmin.notifyDataSetChanged();

    }
}
