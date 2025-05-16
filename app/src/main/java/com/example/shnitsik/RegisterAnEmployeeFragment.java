package com.example.shnitsik;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class RegisterAnEmployeeFragment extends Fragment {
    private EditText searchEditText;
    private ListView usersListView;
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_register_an_employee, container, false);
        this.searchEditText = rootView.findViewById(R.id.search_user);
        this.usersListView = rootView.findViewById(R.id.user_list_view);
        this.database = FirebaseDatabase.getInstance();
        this.usersRef = database.getReference("Root");
        // הוסף את ה-TextWatcher ל-EditText
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // לא צריך לבצע שום פעולה לפני שינוי הטקסט
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // כל פעם שהטקסט משתנה, מבצעים חיפוש מחדש
                String searchQuery = charSequence.toString().trim();
                if (!searchQuery.isEmpty()) {
                    performSearch(searchQuery);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });
        return rootView;
    }

    // פונקציה לביצוע חיפוש ב-Firebase בזמן אמת
    private void performSearch(String searchQuery) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Root").child("Users");
        // חיפוש לפי שם משתמש
        // חיפוש בכל המשתמשים שמתחילים בשם "John" או מכילים אותו
        // ה-\uf8ff הוא תו ייחודי בתקן UTF-8 שמספק הרחבה לחיפוש כך שיכלול את כל המילים
        // שמתחילות ב-"John", גם אם יש להן תוספות בסוף כמו "John123", "Johnny" וכו'.
        // ה-\uf8ff גורם לכך שהחיפוש ימצא כל ערך שמתחיל ב-"John" וכולל תו אחרי המילה.
        usersRef.orderByChild("userName").startAt(searchQuery).endAt(searchQuery + "\uf8ff").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<String> userList = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String userName = snapshot.child("userName").getValue(String.class);
                            String role = (Boolean.TRUE.equals(snapshot.child("role").getValue(boolean.class)))? "Admin" : "User";
                            String user = userName + "\nrole: " + role;
                            userList.add(user);
                        }

                        // עדכון ה-ListView עם התוצאות החדשות
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, userList){
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                TextView text1 = view.findViewById(android.R.id.text1);
                                text1.setText(userList.get(position));
                                return view;
                            }
                        };
                        RegisterAnEmployeeFragment.this.usersListView.setAdapter(adapter);
                        RegisterAnEmployeeFragment.this.usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                                // קבלת שם המשתמש שנלחץ
                                final String username = userList.get(position);
                                // יצירת AlertDialog
                                new AlertDialog.Builder(getContext()).setTitle("Are you sure?").setMessage("Do you want to change the role of user: " + username)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // אם המשתמש לוחץ "Yes", נעדכן את ה-role ב-Firebase ל-true
                                                updateUserRole(username.split("\n")[0]);
                                                Toast.makeText(getContext(), "Role changed to Admin", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // אם המשתמש לוחץ "No", פשוט נסגור את ה-Dialog
                                                dialog.dismiss();
                                            }
                                        })
                                        .show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Error Loading Users", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void updateUserRole(String username) {
        // כאן אנחנו שולפים את המשתמש מה-Databse
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Root/Users");
        // עדכון ה-role ל-true (Admin)
        usersRef.orderByChild("userName").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.e("UpdateRole", "No user found for username: " + username);
                    return;
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // עדכון ה-role של המשתמש ל-"Admin"
                    Boolean currentRole = snapshot.child("role").getValue(Boolean.class);
                    if (currentRole != null) {
                        boolean newRole = !currentRole; // הפוך את הערך
                        snapshot.getRef().child("role").setValue(newRole);
                        Log.d("UpdateRole", "Role updated for user: " + username);
                    } else {
                        Log.e("UpdateRole", "Role not found for user: " + username);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("UpdateRole", "Error updating role: " + databaseError.getMessage());
                Toast.makeText(getContext(), "Error updating role", Toast.LENGTH_SHORT).show();
            }
        });
    }


}