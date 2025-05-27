
package com.example.shnitsik;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Fragments center activity.
 */
public class FragmentsCenterActivity extends AppCompatActivity {
    private FirebaseUser currentUser;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_center);

        // אם זהו הראשון, נניח שנטען את ה-HomeFragment בהתחלה
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new HomeFragment());
            transaction.commit();
        }
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().findItem(R.id.navigation_home).setVisible(false);
        bottomNavigationView.getMenu().findItem(R.id.navigation_edit).setVisible(false);
        bottomNavigationView.getMenu().findItem(R.id.navigation_register_an_employee).setVisible(false);
        bottomNavigationView.getMenu().findItem(R.id.nav_reviews).setVisible(false);
        bottomNavigationView.getMenu().findItem(R.id.nav_previous_orders).setVisible(false);
        checkForAdmin(bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_reviews) {
                selectedFragment = new ReviewsFragment();
            } else if (item.getItemId() == R.id.nav_previous_orders) {
                selectedFragment = new PreviousOrdersFragment();
            }else if (item.getItemId() == R.id.navigation_home) {
                selectedFragment = new HomeFragment();
            }else if (item.getItemId() == R.id.navigation_edit) {
                selectedFragment = new EditInfoFragment();
            }else if (item.getItemId() == R.id.navigation_register_an_employee) {
                selectedFragment = new RegisterAnEmployeeFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });



    }

    /**
     * Load fragment.
     *
     * @param fragment the fragment
     */
// פונקציה להחלפת פרגמנט
    public void loadFragment(Fragment fragment) {
        // יצירת Transaction להחלפת פרגמנט
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);  // R.id.fragment_container הוא ה-Container שבו יטען הפרגמנט
        transaction.addToBackStack(null);  // אם רוצים להחזיר אחורה את הפרגמנט הקודם
        transaction.commit();  // אישור השינוי
    }

    private void checkForAdmin(BottomNavigationView bottomNavigationView) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference roleRef = database.getReference("Root").child("Users").child(userId).child("role");
            roleRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Boolean role = task.getResult().getValue(Boolean.class);
                    if (role==null){
                        Toast.makeText(this, "role is null", Toast.LENGTH_LONG).show();

                    }
                    if (role != null && role) {
                        // אם role == true (אדמין)
                        // הצג את התוכן למנהל
                        setAdminMenuItems(bottomNavigationView);
                    } else {
                        // אם role != true (לא אדמין)
                        // הצג תוכן אחר
                        setUserMenuItems(bottomNavigationView);
                    }
                } else {
                    Toast.makeText(this, "No clear user access permissions", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show();
        }
    }
    private void setUserMenuItems(BottomNavigationView bottomNavigationView) {
        bottomNavigationView.getMenu().findItem(R.id.navigation_home).setVisible(true);
        bottomNavigationView.getMenu().findItem(R.id.navigation_edit).setVisible(false);
        bottomNavigationView.getMenu().findItem(R.id.navigation_register_an_employee).setVisible(false);
        bottomNavigationView.getMenu().findItem(R.id.nav_reviews).setVisible(true);
        bottomNavigationView.getMenu().findItem(R.id.nav_previous_orders).setVisible(true);

    }
    private void setAdminMenuItems(BottomNavigationView bottomNavigationView) {
        bottomNavigationView.getMenu().findItem(R.id.navigation_home).setVisible(true);
        bottomNavigationView.getMenu().findItem(R.id.navigation_edit).setVisible(true);
        bottomNavigationView.getMenu().findItem(R.id.navigation_register_an_employee).setVisible(true);
        bottomNavigationView.getMenu().findItem(R.id.nav_reviews).setVisible(true); // ✅ הצג ביקורות גם לאדמין
        bottomNavigationView.getMenu().findItem(R.id.nav_previous_orders).setVisible(false);
    }

}
