package com.example.shnitsik;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shnitsik.models.Order;
import com.example.shnitsik.models.PreviousOrdersAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PreviousOrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private PreviousOrdersAdapter adapter;
    private List<Order> userOrders = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_previous_orders, container, false);

        recyclerView = view.findViewById(R.id.previousOrdersRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PreviousOrdersAdapter(getContext(), userOrders);
        recyclerView.setAdapter(adapter);

        fetchUserOrders();
        return view;
    }

    private void fetchUserOrders() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        String uid = currentUser.getUid();

        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Root/Orders");
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userOrders.clear();
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    Order order = orderSnap.getValue(Order.class);
                    if (order != null && uid.equals(order.getOrdererUID())) {
                        userOrders.add(order);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading orders", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
