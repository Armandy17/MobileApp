package com.example.mobile_contentsapp.Recipe;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mobile_contentsapp.Profile.RecylcerViewEmpty;
import com.example.mobile_contentsapp.R;
import com.example.mobile_contentsapp.Recipe.Retrofit.RecipeListGet;
import com.example.mobile_contentsapp.Recipe.Retrofit.RecipeSearchClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;

public class RecipeFragment extends Fragment {
    private int start = -1;
    private int categoryNumber;
    private List<RecipeListGet> listGet;
    private ArrayList<RecipeListGet> list;
    private RecipeListAdapter adapter;

    private Spinner categorySpinner;
    private ImageButton searchBtn;
    private RecylcerViewEmpty recipeRecyclerView;
    private TextView emptyText;
    private String keyword;
    private boolean remainList = false;
    private boolean isLoding = false;
    private boolean isCategory = false;


    @Override
    public void onStart() {
        super.onStart();
        if (!isLoding&&isCategory){
            isLoding = true;
            list.clear();
            adapter.notifyDataSetChanged();
            search(-1,null,categoryNumber);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe,container,false);

        recipeRecyclerView = view.findViewById(R.id.recipe_list_recycler);
        searchBtn = view.findViewById(R.id.recipe_search);
        categorySpinner = view.findViewById(R.id.search_category_spinner);

        EditText recipeSearchEdit = view.findViewById(R.id.recipe_search_edit);
        SwipeRefreshLayout swipe = view.findViewById(R.id.recipe_swipe);

        ArrayList<CategoryItem> categoryList = new ArrayList<>();
        setCategory(categoryList);
        CategoryAdapter categoryAdapter = new CategoryAdapter(view.getContext(),categoryList);
        categorySpinner.setAdapter(categoryAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isLoding){
                    isLoding = true;
                    list.clear();
                    adapter.notifyDataSetChanged();
                    categoryNumber = position;
                    search(-1,null,categoryNumber);
                    isCategory = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        recipeSearchEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (!isLoding){
                        list.clear();
                        adapter.notifyDataSetChanged();
                        isLoding = true;
                        keyword = recipeSearchEdit.getText().toString();
                        search(-1,keyword,categoryNumber);
                    }
                    return true;
                }
                return false;
            }
        });

        list = new ArrayList<>();
        LinearLayoutManager manager = new LinearLayoutManager(view.getContext(),LinearLayoutManager.VERTICAL,false);
        recipeRecyclerView.setLayoutManager(manager);
        adapter = new RecipeListAdapter(list);
        recipeRecyclerView.setAdapter(adapter);

        recipeRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoding){
                    if (manager != null && manager.findLastCompletelyVisibleItemPosition() == list.size()-1 &&
                            remainList){
                        Log.d(TAG, "onScrolled: 스크롤");
                        isLoding = true;
                        search(start,keyword,categoryNumber);
                    }
                }
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoding){
                    Log.d(TAG, "onItemSelected: 버튼");
                    isLoding = true;
                    list.clear();
                    adapter.notifyDataSetChanged();
                    keyword = recipeSearchEdit.getText().toString();
                    search(-1,keyword,categoryNumber);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isLoding){
                    isLoding = true;
                    Log.d(TAG, "onItemSelected: 리로딩");
                    categorySpinner.setSelection(0);
                    list.clear();
                    adapter.notifyDataSetChanged();
                    search(-1,null,0);
                    swipe.setRefreshing(false);
                }
            }
        });

        return view;
    }
    private void setCategory(ArrayList<CategoryItem> categoryList){
        categoryList.add(new CategoryItem("전체",0,1));
        categoryList.add(new CategoryItem("커피",1,1));
        categoryList.add(new CategoryItem("음료",2,1));
        categoryList.add(new CategoryItem("디저트",3,1));
        categoryList.add(new CategoryItem("그 외",4,1));

    }

    private void search(int startValue, String keyword, int categoryNumber){
        list.add(null);
        adapter.notifyItemInserted(list.size()-1);

        Call<List<RecipeListGet>> call = RecipeSearchClient
                .getApiService().recipeSearchCall(String.valueOf(startValue),keyword,categoryNumber);
        call.enqueue(new Callback<List<RecipeListGet>>() {
            @Override
            public void onResponse(Call<List<RecipeListGet>> call, Response<List<RecipeListGet>> response) {
                if (!response.isSuccessful()){
                    return;
                }

                listGet = response.body();

                list.remove(list.size()-1);
                adapter.notifyItemRemoved(list.size()-1);

                if (listGet.size() != 0){
                    start = listGet.get(listGet.size()-1).getId();
                    for (int i = 0;  i < listGet.size(); i++){
                        list.add(listGet.get(i));
                    }
                    adapter.notifyDataSetChanged();
                }else{
                    remainList = false;
                }
                recipeRecyclerView.setEmptyView(emptyText);
            }
            @Override
            public void onFailure(Call<List<RecipeListGet>> call, Throwable t) {
            }
        });
        isLoding = false;
    }

}
