package com.example.bookmanagement.Filters;

import android.widget.Filter;

import com.example.bookmanagement.Models.ModelPdf;
import com.example.bookmanagement.adapters.AdapterPdfUser;

import java.util.ArrayList;

public class FilterPdfUser extends Filter {
    ArrayList<ModelPdf> filterList;
    AdapterPdfUser adapterPdfUser;

    public FilterPdfUser(ArrayList<ModelPdf> filterList, AdapterPdfUser adapterPdfUser) {
        this.filterList = filterList;
        this.adapterPdfUser = adapterPdfUser;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        if(charSequence!=null||charSequence.length()>0){
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<ModelPdf> filterModel = new ArrayList<>();

            for(int i=0; i<filterList.size(); i++){
                //validate
                if(filterList.get(i).getTitle().toUpperCase().contains(charSequence)){
                    filterModel.add(filterList.get(i));
                }
            }
            results.count = filterModel.size();
            results.values = filterModel;
        }else{
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        adapterPdfUser.pdfArrayList = (ArrayList<ModelPdf>)filterResults.values;
        adapterPdfUser.notifyDataSetChanged();
    }
}
