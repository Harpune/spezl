package com.example.lukas.spezl.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lukas.spezl.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AGBActivity extends AppCompatActivity {

    private DatabaseReference mRefUser;
    private FirebaseUser fireUser;

    private String privacyPolicy, termsOfUse;
    private String password = "";
    private String uid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agb);

        // Read the strings in method in the end, because they are too long.
        readStrings();

        // Check if intent comes from RegisterActivity or DecisionActivity and enable/disable
        // the "Delete-User"-function.
        Intent intent = getIntent();
        boolean setupToolbar = intent.getBooleanExtra("SETUP_TOOLBAR", false);
        Log.d("SETUP_TOOLBAR", "" + setupToolbar);

        // Sertup the toolbar, if needed.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle(R.string.text_settings);
        if (setupToolbar) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            }
        }

        // Find the views.
        TextView privacyPolicyView = (TextView) findViewById(R.id.privacyPolicy);
        TextView termsOfUseView = (TextView) findViewById(R.id.termsOfUse);

        // Setup the Views.
        privacyPolicyView.setText(privacyPolicy);
        termsOfUseView.setText(termsOfUse);

        // Set RESULT_OK, therefore the user read the AGB.
        setResult(RESULT_OK);
    }

    /**
     * Inflates the menu in the toolbar.
     * @param menu The mnu layout clicked.
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }

    /**
     * Handle clicks on the item of the toolbar.
     * @param item Clicked menu-item.
     * @return boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.delete_user:
                // Ask user if he is sure.
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.pic_owl_active)
                        .setTitle("Acount löschen")
                        .setMessage("Möchtest du wirklich keinen Account löschen? Das kann nicht rückgängig gemacht werden.")
                        .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Show dialog for user input.
                                initializeDialog();
                            }
                        })
                        .setNegativeButton("Nein", null) // nothing when canceled.
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Show the AlertDialog for user input.
     * Get the password of the user.
     */
    public void initializeDialog() {
        fireUser = FirebaseAuth.getInstance().getCurrentUser();// current user.
        mRefUser = FirebaseDatabase.getInstance().getReference("users");// reference to the user node.
        //DatabaseReference mRefEvent = FirebaseDatabase.getInstance().getReference("events"); //reference to the event node.

        // Setup dialog.
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Benutzer löschen");
        alert.setMessage("Geben Sie ihr Passort ein, um Ihren Account zu löschen.");
        alert.setIcon(R.drawable.pic_owl_active);

        // Set an EditText view to get user password.
        final EditText passwordView = new EditText(this);
        passwordView.setHint(R.string.text_password);
        passwordView.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

        alert.setView(passwordView); // Add EditText to alertView.

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                password = passwordView.getText().toString().trim(); // get user input.

                if (password.equals("") || password.length() < 6) { // check the input
                    Toast.makeText(AGBActivity.this, "Gib bitte dein Passwort ein", Toast.LENGTH_LONG).show();
                } else {
                    Log.d("DELETE_USER", "User Email: " + fireUser.getEmail());
                    deleteUser(); // delete user.
                }
            }
        });
        alert.setNegativeButton("Cancel", null); // Do nothing on cancel.
        alert.show();
    }

    /**
     * Method to delete a user.
     */
    public void deleteUser() {
        uid = fireUser.getUid(); // Save uid, because its gone as soon as user is deleted.
        mRefUser.child(uid).removeValue(); // Remove the user-node first. Afterwards the permissions are missing.
        AuthCredential credential = EmailAuthProvider.getCredential(fireUser.getEmail(), password);

        // Reauthenticate the user.
        fireUser.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) { // successfull? -> delete User.
                            fireUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) { // successfill -> SignOut and go to WelcomeActivity.
                                        Log.d("DELETE_USER", "User account deleted: " + uid);
                                        Toast.makeText(AGBActivity.this, "Benutzer wurde gelöscht!", Toast.LENGTH_LONG).show();

                                        FirebaseAuth.getInstance().signOut();
                                        Intent intent = new Intent(AGBActivity.this, WelcomeActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear every Activity.
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(AGBActivity.this, "Benutzer konnte nicht gelöscht werden... Versuchen Sie es erneut.", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(AGBActivity.this, "Das war nicht dein Passwort", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Handles the toolbar-onBackPress.
     * @return boolean.
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Strings saved in code for format sake.
     */
    public void readStrings() {
        privacyPolicy = "Datenschutzerklärung App-Spezl\n" +
                "\n" +
                "\n" +
                "Der Schutz und die Sicherheit von persönlichen Daten hat bei uns eine hohe Priorität. Daher halten wir uns strikt an die Regeln des deutschen Bundesdatenschutzgesetzes (BDSG). Nachfolgend werden Sie darüber informiert, welche Art von Daten erfasst und zu welchem Zweck sie erhoben werden.\n" +
                "\n" +
                "1. Datenübermittlung/Datenprotokollierung\n" +
                "\n" +
                "Beim Besuch dieser Seite verzeichnet der Web-Server automatisch Log-Files, die keiner bestimmten Person zugeordnet werden können. Diese Daten beinhalten z.B.\n" +
                "\n" +
                "-\tDen Browsertyp und - version\n" +
                "-\tWebsite, von der aus Sie uns besuchen (Referrer URL)\n" +
                "-\tDatum und Uhrzeit ihres Zugriffes\n" +
                "-\tIhre Internet Protokoll (IP)-Adresse\n" +
                "\n" +
                "Diese Daten werden nur zum Zweck der statistischen Auswertung gesammelt. Eine Weitergabe an, zu kommerziellen und nicht kommerziellen Zwecken, findet nicht statt.\n" +
                "\n" +
                "2. Nutzung persönlicher Daten\n" +
                "\n" +
                "Persönliche Daten werden nur erhoben oder verarbeitet, wenn Sie diese Angaben freiwillig, z.B. im Rahmen einer Registrierung oder Erstellung eines Events. Sofern keine erforderlichen Gründe im Zusammenhang einer Geschäftsabwicklung bestehen, können Sie jederzeit die zuvor erteilte Genehmigung Ihrer Persönlichen Datenspeicherung mit sofortiger Wirkung schriftlich (E-Mail oder postalisch) widerrufen. Ihre Daten werden nicht an Dritte weitergegeben, es sei denn, eine Weitergabe ist aufgrund gesetzlicher Vorschriften erforderlich.\n" +
                "\n" +
                "3. Auskunft, Änderung und Löschung ihrer Daten\n" +
                "\n" +
                "Sie können aufgrund des Bundesdatenschutzgesetzes jederzeit bei uns schriftlich nachfragen, ob und welche personenbezogene Daten bei uns über Sie gespeichert sind. Eine entsprechende Mitteilung hierzu erhalten Sie umgehend.\n" +
                "\n" +
                "4. Sicherheit ihrer Daten\n" +
                "\n" +
                "Ihre uns zu Verfügung gestellten persönlichen Daten werden durch Ergreifung aller technischen und organisatorischen Maßnahmen basierend auf den 8. Grundsätzen im BDSG, so gesichert, dass sie für den Zugriff unberechtigter Dritter unzugänglich sind.\n" +
                "\n" +
                "5.Änderungen dieser Datenschutzbestimmungen\n" +
                " \n" +
                "Wir werden diese Richtlinien zum Schutz Ihrer persönlichen Daten von Zeit zu Zeit aktualisieren. Sie sollten sich diese Richtlinien gelegentlich ansehen, um auf dem Laufenden darüber zu bleiben, wie wir Ihre Daten schützen und die Inhalte unserer Website stetig verbessern. Sollten wir wesentliche Änderungen bei der Sammlung, der Nutzung und/oder der Weitergabe der uns von Ihnen zur Verfügung gestellten personenbezogenen Daten vornehmen, werden wir Sie durch einen eindeutigen und gut sichtbaren Hinweis auf der Website darauf aufmerksam machen. Mit der Nutzung der Webseite erklären Sie sich mit den Bedingungen dieser Richtlinien zum Schutz persönlicher Daten einverstanden.Bei Fragen zu diesen Datenschutzbestimmungen wenden Sie sich bitte über unsere Kontakt-Seite an uns.\n" +
                "\n";
        termsOfUse = "Nutzungsbedingungen Spezl\n" +
                "\n" +
                "Willkommen bei Spezl!\n" +
                " \n" +
                "Vielen Dank, dass Du dich für Spezl entschieden hast. Wenn Du die App nutzt, bist Du verpflichtet dich an unsere Nutzungsbedingungen zu halten.\n" +
                "\n" +
                "\n" +
                "Allgemeine Nutzung von Spezl\n" +
                "\n" +
                "Die Nutzungsbedingungen sind als Ergänzung der Nutzungsbedingungen des Google Play Stores zu betrachten. \n" +
                "\n" +
                "Bitte verwende unsere App nicht auf missbräuchliche Art und Weise. Du bist beispielsweise nicht berechtigt auf unsere App zuzugreifen außerhalb der von uns bereitgestellten Oberfläche. Darüber hinaus darf unsere App nur innerhalb der gesetzlichen Rahmenbedingungen der Bundesrepublik Deutschland. Das Team Spezl nimmt sich raus, die Nutzung für jeden einzelnen Nutzer auszusetzen oder einzustellen, wenn Du gegen die Nutzungsrichtlinien verstoßt oder ein mutmaßliches Fehlverhalten vermutet wird.\n" +
                "\n" +
                "Durch die Nutzung unserer App erhältst du keinerlei Urheberechte oder gewerbliche Schutzrechte an unserer App. Du darfst unsere Logo nutzen, für die Veranstaltung eines Events. Dieses Event hat sich an alle lokalen gesetzlichen Vorschriften zu halten. \n" +
                "Wir behalten uns vor, Inhalte deines Profils und deiner erstellten Events auf eine mögliche Rechtswidrigkeit oder Verstöße gegen unsere Richtlinien zu prüfen und gegeben falls rechtliche Schritte einzuleiten. Darüber hinaus können wir Inhalte entfernen oder deren Darstellung ablehnen, wenn diese gegen unsere Richtlinien oder geltendes Recht verstoßen\n" +
                "\n" +
                "Unser Dienst ist auf mobilen Endgeräten verfügbar. Bitte nutze den Dienst nicht in einer Weise, die dich ablenkt und das Einhalten von Verkehrsregeln oder Sicherheitsvorschriften verhindert.\n" +
                "Das Team Spezl nimmt sich raus, die Nutzungsbedingungen jederzeit zu ändern oder anzupassen. Um beispielsweise auf rechtliche Rahmenbedingungen oder Produktänderungen reagieren zu können.\n" +
                "Deine Pflicht ist es die Nutzungsbedingungen daher regelmäßig zu überprüfen. Jegliche Änderungen hinsichtlich einer neuen Funktion sind sofort wirksam. Stimmst Du diesen nicht zu, musst Du den Dienst beenden.\n" +
                "Der Gerichtsstand für sämtliche Streitigkeiten ist Heidenheim an der Brenz.\n" +
                "\n" +
                "Deine Inhalte in unserer App\n" +
                "\n" +
                "In deinem Profil kannst Du persönliche Inhalte einstellen. Du behältst die Rechte als Urheber und alle gewerbliche Schutzrechte an den Inhalten, die Du bei Spezl einstellst. \n" +
                "Indem Du deine Daten in Form von urheberrechtlichen oder sonst rechtlich geschützten Inhalte in unseren Dienst einstellst, räumst du dem Team Spezl und den dazugehörigen Vertragspartnern unentgeltlich die notwendige, nicht ausschließliche, weltweiten und zeitlich unbegrenzten Rechte ein, diese Inhalte ausschließlich zur Erbringung der jeweiligen Leistung innerhalb der App und dem lediglich in dem dafür nötigen Umfang zu verwenden. Damit das Team Spezl den Dienst anbieten kann, müssen deine Inhalte gespeichert werden und auf einem Server gehostet werden. Das Nutzungsrecht umfasst daher das Recht, die Inhalte technisch zu vervielfältigen.Darüber hinaus räumt sich das Team Spezl das Recht der öffentlichen Zugänglichmachung deiner Inhalte, ausschließlich für den Fall, dass Du wegen der Natur des Dienstes (z.B. Veröffentlichung der Teilnehmer eines Events) eine Veröffentlichung beabsichtigen oder Du ausdrücklich einer Zugänglichmachung bestimmt hast. Durch die Teilnahme an einem Event nimmt sich Team Spezl vor, deine Inhalte in Teilen oder in Ganzen in dem dafür notwendig nötigen Teilnehmerkreis zu veröffentlichen.\n" +
                "Das Recht der öffentlichen Zugänglichmachung endet mit der Löschung deiner Daten oder Du die Bestimmung der öffentlichen Zugänglichmachung aufhebst.\n" +
                "\n" +
                "Änderung und Beendigung unseres Spezl-Dienstes\n" +
                "\n" +
                "Unser Bestreben ist es, unseren Dienst laufend zu optimieren und zu verändern. So ist es für uns möglich, unter Berücksichtigung der jeweiligen Interessen Funktionen und Features hinzuzufügen oder zu entfernen. Zudem können bei Bedarf neue Beschränkungen hinzugefügt werden.\n" +
                "Du kannst die Nutzung unseres Dienstes jederzeit beenden.\n" +
                "\n" +
                "Haftung für unseren Dienst\n" +
                "\n" +
                "Bei Vorsatz und grober Fahrlässigkeit, auch der gesetzlichen Vertreter und Erfüllungsgehilfen, haftest Du und das Team Spezl nach den gesetzlichen Bestimmungen. Für das Auftreten von Personen- und Sachschäden sowie im Falle arglistiger Täuschung innerhalb eines Events übernimmt das Team Spezl keine Haftung. Das Team Spezl fungiert hier lediglich als Vermittler verschiedenen Interessensgruppen.\n" +
                "Bei durch Dich oder Team Spezl, deren gesetzlichen Vertreter oder Erfüllungsgehilfen leicht fahrlässig verursachten Sach-und Vermögensschäden ist die Haftung nach den gesetzlichen Anforderungen beschränkt.\n" +
                "\n";

    }
}
