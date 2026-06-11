@file:Suppress("DEPRECATION")
package com.chinmaib.sportconnect.auth

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.chinmaib.sportconnect.auth.components.PlayerSelectionStep
import com.chinmaib.sportconnect.auth.components.ProfileInfoStep
import com.chinmaib.sportconnect.auth.components.SportsSelectionStep
import com.chinmaib.sportconnect.ui.theme.*

enum class SetupStep {
    PROFILE_INFO,
    SPORTS_SELECTION,
    PLAYER_SELECTION
}

data class FamousPlayer(val name: String, val sport: String, val imageUrl: String? = null)
data class Sport(val name: String, val imageUrl: String)

@Composable
fun ProfileSetupScreen(
    userName: String,
    onComplete: (Uri?, String, String, List<String>, List<String>) -> Unit,
) {
    var currentStep by remember { mutableStateOf(SetupStep.PROFILE_INFO) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedDob by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    val selectedSports = remember { mutableStateListOf<String>() }
    val selectedPlayers = remember { mutableStateListOf<String>() }

    val filteredPlayers = remember(selectedSports.toList()) {
        allPlayers.filter { selectedSports.contains(it.sport) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepForestNightStart, DeepForestNightEnd)
                )
            ),
    ) {
        when (currentStep) {
            SetupStep.PROFILE_INFO -> {
                ProfileInfoStep(
                    userName = userName,
                    selectedImageUri = selectedImageUri,
                    onImageSelected = { selectedImageUri = it },
                    selectedDob = selectedDob,
                    onDobSelected = { selectedDob = it },
                    phoneNumber = phoneNumber,
                    onPhoneChanged = { phoneNumber = it },
                    onNext = {
                        currentStep = SetupStep.SPORTS_SELECTION
                    }
                )
            }
            SetupStep.SPORTS_SELECTION -> {
                SportsSelectionStep(
                    availableSports = availableSports,
                    selectedSports = selectedSports,
                    onBack = { currentStep = SetupStep.PROFILE_INFO },
                    onNext = {
                        currentStep = SetupStep.PLAYER_SELECTION
                    }
                )
            }
            SetupStep.PLAYER_SELECTION -> {
                PlayerSelectionStep(
                    players = filteredPlayers,
                    selectedPlayers = selectedPlayers,
                    onBack = { currentStep = SetupStep.SPORTS_SELECTION },
                    onFinish = { onComplete(selectedImageUri, selectedDob, phoneNumber, selectedSports.toList(), selectedPlayers.toList()) },
                    onSkip = { onComplete(selectedImageUri, selectedDob, phoneNumber, selectedSports.toList(), emptyList()) }
                )
            }
        }
    }
}

val availableSports = listOf(
    Sport(
        name = "Football",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/ad/Football_in_Bloomington%2C_Indiana%2C_2022.jpg/200px-Football_in_Bloomington%2C_Indiana%2C_2022.jpg"
    ),
    Sport(
        name = "Basketball",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7a/Basketball.png/200px-Basketball.png"
    ),
    Sport(
        name = "Cricket",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5f/Cricket_ball.jpg/200px-Cricket_ball.jpg"
    ),
    Sport(
        name = "Volleyball",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/62/Volleyball_Brazil_Russia_2008_Olympics.jpg/200px-Volleyball_Brazil_Russia_2008_Olympics.jpg"
    ),
    Sport(
        name = "Tennis",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3e/Female_tennis_player.jpg/200px-Female_tennis_player.jpg"
    ),
    Sport(
        name = "Badminton",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/54/Badminton_Smash4.jpg/200px-Badminton_Smash4.jpg"
    ),
    Sport(
        name = "Hockey",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d2/Field_hockey.jpg/200px-Field_hockey.jpg"
    ),
    Sport(
        name = "Kabaddi",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/ca/Kabaddi_India.jpg/200px-Kabaddi_India.jpg"
    )
)

val allPlayers = listOf(
    // ── CRICKET ──────────────────────────────────────────────────────────────
    FamousPlayer("Virat Kohli", "Cricket",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9d/Virat_Kohli_-_2020.jpg/200px-Virat_Kohli_-_2020.jpg"),
    FamousPlayer("Sachin Tendulkar", "Cricket",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c9/Sachin_Tendulkar_at_the_WEF%2C_2013.jpg/200px-Sachin_Tendulkar_at_the_WEF%2C_2013.jpg"),
    FamousPlayer("MS Dhoni", "Cricket",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/MS_Dhoni_in_2019.jpg/200px-MS_Dhoni_in_2019.jpg"),
    FamousPlayer("Rohit Sharma", "Cricket",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Rohit_Sharma.jpg/200px-Rohit_Sharma.jpg"),
    FamousPlayer("Hardik Pandya", "Cricket", null),
    FamousPlayer("Jasprit Bumrah", "Cricket",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/0/00/Jasprit_Bumrah_T20.jpg/200px-Jasprit_Bumrah_T20.jpg"),
    FamousPlayer("KL Rahul", "Cricket", null),
    FamousPlayer("Shubman Gill", "Cricket", null),
    FamousPlayer("Yuvraj Singh", "Cricket",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4c/Yuvraj_Singh_2011_WC.jpg/200px-Yuvraj_Singh_2011_WC.jpg"),
    FamousPlayer("Rishabh Pant", "Cricket", null),

    // ── FOOTBALL ─────────────────────────────────────────────────────────────
    FamousPlayer("Lionel Messi", "Football",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b4/Lionel-Messi-Argentina-2022-FIFA-World-Cup_%28cropped%29.jpg/200px-Lionel-Messi-Argentina-2022-FIFA-World-Cup_%28cropped%29.jpg"),
    FamousPlayer("Cristiano Ronaldo", "Football",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8c/Cristiano_Ronaldo_2018.jpg/200px-Cristiano_Ronaldo_2018.jpg"),
    FamousPlayer("Neymar Jr", "Football", null),
    FamousPlayer("Kylian Mbappé", "Football",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/5/57/2019-07-17_SG_Dynamo_Dresden_v_Paris_Saint-Germain_FC_by_Sandro_Halank_%E2%80%93_096_%28cropped%29.jpg/200px-2019-07-17_SG_Dynamo_Dresden_v_Paris_Saint-Germain_FC_by_Sandro_Halank_%E2%80%93_096_%28cropped%29.jpg"),
    FamousPlayer("Sunil Chhetri", "Football",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/3/37/Sunil_Chhetri.jpg/200px-Sunil_Chhetri.jpg"),
    FamousPlayer("Erling Haaland", "Football",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/8/83/Erling_Haaland_2022.jpg/200px-Erling_Haaland_2022.jpg"),
    FamousPlayer("Gurpreet Singh Sandhu", "Football", null),
    FamousPlayer("Sandesh Jhingan", "Football", null),
    FamousPlayer("Sahal Abdul Samad", "Football", null),
    FamousPlayer("Ashique Kuruniyan", "Football", null),

    // ── BASKETBALL ───────────────────────────────────────────────────────────
    FamousPlayer("LeBron James", "Basketball",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/LeBron_James_crop.jpg/200px-LeBron_James_crop.jpg"),
    FamousPlayer("Stephen Curry", "Basketball",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/3/34/Stephen_Curry_Shooting_%28cropped%29.jpg/200px-Stephen_Curry_Shooting_%28cropped%29.jpg"),
    FamousPlayer("Kevin Durant", "Basketball", null),
    FamousPlayer("Giannis Antetokounmpo", "Basketball",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9b/Giannis_Antetokounmpo_2019.jpg/200px-Giannis_Antetokounmpo_2019.jpg"),
    FamousPlayer("Kyrie Irving", "Basketball", null),
    FamousPlayer("Russell Westbrook", "Basketball", null),
    FamousPlayer("Ja Morant", "Basketball", null),
    FamousPlayer("Luka Dončić", "Basketball",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/5/55/Luka_Don%C4%8Di%C4%87_2022_%28cropped%29.jpg/200px-Luka_Don%C4%8Di%C4%87_2022_%28cropped%29.jpg"),
    FamousPlayer("Satnam Singh", "Basketball", null),
    FamousPlayer("Princepal Singh", "Basketball", null),

    // ── VOLLEYBALL ───────────────────────────────────────────────────────────
    FamousPlayer("Key Alves", "Volleyball", null),
    FamousPlayer("Zehra Gunes", "Volleyball", null),
    FamousPlayer("Gabi Guimarães", "Volleyball", null),
    FamousPlayer("Ebrar Karakurt", "Volleyball", null),
    FamousPlayer("Ran Takahashi", "Volleyball", null),
    FamousPlayer("Yuki Ishikawa", "Volleyball", null),
    FamousPlayer("Alyssa Valdez", "Volleyball", null),
    FamousPlayer("Melissa Vargas", "Volleyball", null),
    FamousPlayer("Ashwal Rai", "Volleyball", null),
    FamousPlayer("Mohan Ukkrapandian", "Volleyball", null),

    // ── TENNIS ───────────────────────────────────────────────────────────────
    FamousPlayer("Rafael Nadal", "Tennis",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8e/Rafael_Nadal_Roland_Garros_2022_crop.jpg/200px-Rafael_Nadal_Roland_Garros_2022_crop.jpg"),
    FamousPlayer("Novak Djokovic", "Tennis",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Novak_Djokovi%C4%87_2022.jpg/200px-Novak_Djokovi%C4%87_2022.jpg"),
    FamousPlayer("Roger Federer", "Tennis",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/4/40/Roger_Federer_2020_%28cropped%29.jpg/200px-Roger_Federer_2020_%28cropped%29.jpg"),
    FamousPlayer("Serena Williams", "Tennis",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/Serena_Williams_US_Open_2013_crop.jpg/200px-Serena_Williams_US_Open_2013_crop.jpg"),
    FamousPlayer("Sania Mirza", "Tennis",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fd/Sania_Mirza_%28cropped%29.jpg/200px-Sania_Mirza_%28cropped%29.jpg"),
    FamousPlayer("Carlos Alcaraz", "Tennis",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/9/98/Carlos_Alcaraz_2022_Wimbledon_%28cropped%29.jpg/200px-Carlos_Alcaraz_2022_Wimbledon_%28cropped%29.jpg"),
    FamousPlayer("Iga Świątek", "Tennis", null),
    FamousPlayer("Leander Paes", "Tennis", null),
    FamousPlayer("Rohan Bopanna", "Tennis", null),
    FamousPlayer("Sumit Nagal", "Tennis", null),

    // ── BADMINTON ────────────────────────────────────────────────────────────
    FamousPlayer("PV Sindhu", "Badminton",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/9/90/PV_Sindhu_%28cropped%29.jpg/200px-PV_Sindhu_%28cropped%29.jpg"),
    FamousPlayer("Saina Nehwal", "Badminton",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Saina_Nehwal_BWF_2013.jpg/200px-Saina_Nehwal_BWF_2013.jpg"),
    FamousPlayer("Lee Chong Wei", "Badminton",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/0/04/Lee_Chong_Wei_%28cropped%29.jpg/200px-Lee_Chong_Wei_%28cropped%29.jpg"),
    FamousPlayer("Viktor Axelsen", "Badminton", null),
    FamousPlayer("Tai Tzu Ying", "Badminton", null),
    FamousPlayer("Lakshya Sen", "Badminton", null),
    FamousPlayer("Kidambi Srikanth", "Badminton", null),
    FamousPlayer("Chirag Shetty", "Badminton", null),
    FamousPlayer("Satwiksairaj Rankireddy", "Badminton", null),
    FamousPlayer("Gayatri Gopichand", "Badminton", null),

    // ── HOCKEY ───────────────────────────────────────────────────────────────
    FamousPlayer("PR Sreejesh", "Hockey",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d5/PR_Sreejesh.jpg/200px-PR_Sreejesh.jpg"),
    FamousPlayer("Harmanpreet Singh", "Hockey", null),
    FamousPlayer("Manpreet Singh", "Hockey", null),
    FamousPlayer("Savita Punia", "Hockey", null),
    FamousPlayer("Rani Rampal", "Hockey",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Rani_Rampal.jpg/200px-Rani_Rampal.jpg"),
    FamousPlayer("Hardik Singh", "Hockey", null),
    FamousPlayer("Vandana Katariya", "Hockey", null),
    FamousPlayer("Salima Tete", "Hockey", null),
    FamousPlayer("Vivek Sagar Prasad", "Hockey", null),
    FamousPlayer("Lalremsiami", "Hockey", null),

    // ── KABADDI ──────────────────────────────────────────────────────────────
    FamousPlayer("Pawan Sehrawat", "Kabaddi", null),
    FamousPlayer("Pardeep Narwal", "Kabaddi", null),
    FamousPlayer("Rahul Chaudhari", "Kabaddi", null),
    FamousPlayer("Ajay Thakur", "Kabaddi", null),
    FamousPlayer("Naveen Kumar", "Kabaddi", null),
    FamousPlayer("Arjun Deshwal", "Kabaddi", null),
    FamousPlayer("Aslam Inamdar", "Kabaddi", null),
    FamousPlayer("Anup Kumar", "Kabaddi", null),
    FamousPlayer("Khushdeep Singh Gill", "Kabaddi", null),
    FamousPlayer("Mohit Goyat", "Kabaddi", null)
)
