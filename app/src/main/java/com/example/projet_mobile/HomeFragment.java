package com.example.projet_mobile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private RecyclerView blog_list_view;
    private List<BlogPost> blog_list;
    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    FirebaseAuth firebaseAuth;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageLoad = true;


    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home,container,false);

        blog_list = new ArrayList<>();
        blog_list_view=view.findViewById(R.id.blog_list_view);
        firebaseAuth = FirebaseAuth.getInstance();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);

        if(firebaseAuth.getCurrentUser() != null){
            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if(reachedBottom){
                        String desc = lastVisible.getString("desc");

                        Toast.makeText(container.getContext(), "Reached : "+desc, Toast.LENGTH_SHORT).show();

                        loadMorePost();
                    }


                }
            });

        firebaseFirestore = FirebaseFirestore.getInstance();

        Query  firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);
        firstQuery.addSnapshotListener((value, error) -> {
            if(isFirstPageLoad){
                lastVisible =value.getDocuments().get(value.size() -1);

            }

            for(DocumentChange doc:value.getDocumentChanges()){

                if(doc.getType() == DocumentChange.Type.ADDED){
                    String blogPostId = doc.getDocument().getId();

                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                    if(isFirstPageLoad){
                        blog_list.add(blogPost);

                    }
                    else{
                        blog_list.add(0,blogPost);
                    }

                    blogRecyclerAdapter.notifyDataSetChanged();
                }
            }
            isFirstPageLoad = false;

        });
        }

        return view;
    }
    public void loadMorePost(){
        Query  nextQuery = firebaseFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);
        nextQuery.addSnapshotListener((value, error) -> {
            if(!value.isEmpty()){
            lastVisible =value.getDocuments().get(value.size() -1);
            for(DocumentChange doc:value.getDocumentChanges()){
                if(doc.getType() == DocumentChange.Type.ADDED){
                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);
                    blog_list.add(blogPost);
                    blogRecyclerAdapter.notifyDataSetChanged();
                }
            }
            }
        });

    }

    public  void loadPost(String query){
        System.out.println("hi from loadpost");

        CollectionReference docRef = firebaseFirestore.collection("Posts");


        docRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {


                             blog_list.clear();
                             for (QueryDocumentSnapshot document : task.getResult()) {
                                 if(document.get("desc").toString().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)) ){

                                     BlogPost blogPost = document.toObject(BlogPost.class);

                                     System.out.println(blogPost.getDesc());
                                     blog_list.add(blogPost);
                                     blogRecyclerAdapter.notifyDataSetChanged();

                                 }

                             }


                        } else {

                        }
                    }
                });

    }

    public  void loadPost(){
        System.out.println("hi from loadpost with out query");

        CollectionReference docRef = firebaseFirestore.collection("Posts");


        docRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            System.out.println("before for");
                            blog_list.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {


                                    BlogPost blogPost = document.toObject(BlogPost.class);

                                    System.out.println(blogPost.getDesc());
                                    blog_list.add(blogPost);
                                    blogRecyclerAdapter.notifyDataSetChanged();





                            }
                        } else {

                        }
                    }
                });

    }

}