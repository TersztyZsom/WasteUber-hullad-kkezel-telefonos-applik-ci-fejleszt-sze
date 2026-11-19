package hu.wasteuber.app.ui.screens.subscription

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.wasteuber.app.R
import hu.wasteuber.app.data.model.SubscriptionPlan
import hu.wasteuber.app.data.model.SubscriptionPlans
import hu.wasteuber.app.viewmodel.SubscriptionViewModel

@Composable
fun SubscriptionScreen(
    subscriptionViewModel: SubscriptionViewModel,
    onBackClick: () -> Unit
) {
    var selectedPackage by remember {
        mutableStateOf<String?>(null)
    }

    var selectedPeriod by remember {
        mutableStateOf(
            SubscriptionPlans.PERIOD_MONTHLY
        )
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var message by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(16.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 900.dp)
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )
                ) {

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clipToBounds(),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Image(
                            painter =
                                painterResource(
                                    id =
                                        R.drawable.wasteuber_logo
                                ),

                            contentDescription =
                                "WasteUber logó",

                            contentScale =
                                ContentScale.Fit,

                            modifier = Modifier
                                .size(64.dp)
                                .scale(3.2f)
                        )
                    }

                    Column {

                        Text(
                            text =
                                "WasteUber",

                            style =
                                MaterialTheme.typography.titleMedium,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text =
                                "Előfizetések",

                            style =
                                MaterialTheme.typography.headlineSmall,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick =
                        onBackClick,

                    enabled =
                        !isLoading
                ) {

                    Text(
                        "Vissza"
                    )
                }
            }

            Text(
                text =
                    "Válaszd ki a számodra megfelelő hulladékszállítási csomagot.",

                modifier =
                    Modifier.padding(
                        top = 16.dp
                    )
            )

            Text(
                text =
                    "Minden sikeresen feldolgozott előfizetés " +
                            "+${SubscriptionPlans.SUBSCRIPTION_REPUTATION_REWARD} " +
                            "reputációs pontot ad.",

                modifier =
                    Modifier.padding(
                        top = 8.dp
                    )
            )

            HorizontalDivider(
                modifier =
                    Modifier.padding(
                        top = 20.dp,
                        bottom = 20.dp
                    )
            )

            Text(
                text =
                    "Fizetési időszak"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 12.dp
                    ),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {

                if (
                    selectedPeriod ==
                    SubscriptionPlans.PERIOD_WEEKLY
                ) {

                    Button(
                        onClick = {
                            selectedPeriod =
                                SubscriptionPlans.PERIOD_WEEKLY

                            message = ""
                        },

                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Text(
                            "Heti"
                        )
                    }

                } else {

                    OutlinedButton(
                        onClick = {
                            selectedPeriod =
                                SubscriptionPlans.PERIOD_WEEKLY

                            message = ""
                        },

                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Text(
                            "Heti"
                        )
                    }
                }

                if (
                    selectedPeriod ==
                    SubscriptionPlans.PERIOD_MONTHLY
                ) {

                    Button(
                        onClick = {
                            selectedPeriod =
                                SubscriptionPlans.PERIOD_MONTHLY

                            message = ""
                        },

                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Text(
                            "Havi"
                        )
                    }

                } else {

                    OutlinedButton(
                        onClick = {
                            selectedPeriod =
                                SubscriptionPlans.PERIOD_MONTHLY

                            message = ""
                        },

                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Text(
                            "Havi"
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        20.dp
                    )
            )

            SubscriptionPlans
                .allPlans
                .forEach { plan ->

                    SubscriptionPlanCard(
                        plan =
                            plan,

                        selectedPeriod =
                            selectedPeriod,

                        selected =
                            selectedPackage ==
                                    plan.packageCode,

                        onSelect = {

                            selectedPackage =
                                plan.packageCode

                            message =
                                ""
                        }
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                16.dp
                            )
                    )
                }

            selectedPackage?.let { packageCode ->

                val selectedPlan =
                    SubscriptionPlans.getPlan(
                        packageCode
                    )

                if (
                    selectedPlan != null
                ) {

                    val price =
                        SubscriptionPlans.getPrice(
                            packageCode =
                                selectedPlan.packageCode,

                            period =
                                selectedPeriod
                        )

                    Card(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    16.dp
                                )
                        ) {

                            Text(
                                text =
                                    "Kiválasztott előfizetés"
                            )

                            Text(
                                text =
                                    selectedPlan.name,

                                modifier =
                                    Modifier.padding(
                                        top = 8.dp
                                    )
                            )

                            Text(
                                text =
                                    if (
                                        selectedPeriod ==
                                        SubscriptionPlans.PERIOD_WEEKLY
                                    ) {
                                        "Időszak: Heti"
                                    } else {
                                        "Időszak: Havi"
                                    },

                                modifier =
                                    Modifier.padding(
                                        top = 6.dp
                                    )
                            )

                            Text(
                                text =
                                    "Ár: $price Ft",

                                modifier =
                                    Modifier.padding(
                                        top = 6.dp
                                    )
                            )

                            Text(
                                text =
                                    "Csomag tag: ${selectedPlan.tag}",

                                modifier =
                                    Modifier.padding(
                                        top = 6.dp
                                    )
                            )
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.height(
                                16.dp
                            )
                    )
                }
            }

            if (
                message.isNotBlank()
            ) {

                Card(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        text =
                            message,

                        modifier =
                            Modifier.padding(
                                16.dp
                            )
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )
            }

            HorizontalDivider(
                modifier =
                    Modifier.padding(
                        top = 4.dp,
                        bottom = 20.dp
                    )
            )

            Text(
                text =
                    "Fizetés"
            )

            Text(
                text =
                    "A valódi online fizetési szolgáltató még nincs bekötve. " +
                            "A végleges verzióban innen indul majd a fizetési folyamat.",

                modifier =
                    Modifier.padding(
                        top = 8.dp
                    )
            )

            Button(
                onClick = {
                    /*
                     * Később itt indítjuk el
                     * a valódi fizetési folyamatot.
                     *
                     * Sikeres fizetés után a szolgáltatótól
                     * kapott transactionId kerül majd átadásra:
                     *
                     * subscriptionViewModel.activateSubscription(
                     *     packageCode = ...,
                     *     period = ...,
                     *     transactionId = ...,
                     *     onSuccess = ...,
                     *     onError = ...
                     * )
                     */
                },

                enabled =
                    false,

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 12.dp
                    )
            ) {

                val selectedPlan =
                    selectedPackage?.let {
                        SubscriptionPlans.getPlan(
                            it
                        )
                    }

                if (
                    selectedPlan == null
                ) {

                    Text(
                        text =
                            "Fizetés – válassz csomagot"
                    )

                } else {

                    val price =
                        SubscriptionPlans.getPrice(
                            packageCode =
                                selectedPlan.packageCode,

                            period =
                                selectedPeriod
                        )

                    Text(
                        text =
                            "Fizetés – $price Ft"
                    )
                }
            }

            Text(
                text =
                    "A fizetési gomb addig nem használható, " +
                            "amíg nincs valódi fizetési szolgáltató integrálva.",

                modifier =
                    Modifier.padding(
                        top = 8.dp
                    )
            )

            HorizontalDivider(
                modifier =
                    Modifier.padding(
                        top = 24.dp,
                        bottom = 20.dp
                    )
            )

            Card(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            16.dp
                        )
                ) {

                    Text(
                        text =
                            "Fejlesztői teszt"
                    )

                    Text(
                        text =
                            "Ez a rész kizárólag fejlesztési és tesztelési célra szolgál.",

                        modifier =
                            Modifier.padding(
                                top = 8.dp
                            )
                    )

                    Text(
                        text =
                            "A tesztaktiválás valódi fizetés nélkül aktiválja a csomagot.",

                        modifier =
                            Modifier.padding(
                                top = 6.dp
                            )
                    )

                    Text(
                        text =
                            "Minden tesztaktiválás új teszt tranzakciót hoz létre, " +
                                    "ezért jelenleg minden alkalommal " +
                                    "+${SubscriptionPlans.SUBSCRIPTION_REPUTATION_REWARD} " +
                                    "reputációs pontot is ad.",

                        modifier =
                            Modifier.padding(
                                top = 6.dp
                            )
                    )

                    if (
                        isLoading
                    ) {

                        CircularProgressIndicator(
                            modifier =
                                Modifier
                                    .align(
                                        Alignment.CenterHorizontally
                                    )
                                    .padding(
                                        16.dp
                                    )
                        )

                    } else {

                        Button(
                            onClick = {

                                val packageCode =
                                    selectedPackage

                                if (
                                    packageCode == null
                                ) {

                                    message =
                                        "Először válassz egy csomagot."

                                    return@Button
                                }

                                isLoading =
                                    true

                                message =
                                    ""

                                /*
                                 * FEJLESZTŐI TESZT AKTIVÁLÁS.
                                 *
                                 * Ez a ViewModel régi overloadját
                                 * használja, amelyhez a repository
                                 * automatikusan új teszt
                                 * transactionId-t generál.
                                 */
                                subscriptionViewModel
                                    .activateSubscription(
                                        packageCode =
                                            packageCode,

                                        period =
                                            selectedPeriod,

                                        onSuccess = {

                                            isLoading =
                                                false

                                            val plan =
                                                SubscriptionPlans
                                                    .getPlan(
                                                        packageCode
                                                    )

                                            message =
                                                "${plan?.name ?: "Előfizetés"} " +
                                                        "teszt módban sikeresen aktiválva. " +
                                                        "+${SubscriptionPlans.SUBSCRIPTION_REPUTATION_REWARD} " +
                                                        "reputációs pont."
                                        },

                                        onError = { error ->

                                            isLoading =
                                                false

                                            message =
                                                error
                                        }
                                    )
                            },

                            enabled =
                                selectedPackage != null,

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = 16.dp
                                )
                        ) {

                            val selectedPlan =
                                selectedPackage?.let {
                                    SubscriptionPlans
                                        .getPlan(
                                            it
                                        )
                                }

                            if (
                                selectedPlan == null
                            ) {

                                Text(
                                    text =
                                        "Válassz csomagot"
                                )

                            } else {

                                val price =
                                    SubscriptionPlans.getPrice(
                                        packageCode =
                                            selectedPlan.packageCode,

                                        period =
                                            selectedPeriod
                                    )

                                Text(
                                    text =
                                        "FEJLESZTŐI TESZT AKTIVÁLÁS – $price Ft"
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text =
                    "Éles kiadás előtt a fejlesztői tesztaktiválást el kell távolítani " +
                            "vagy csak fejlesztői buildben szabad engedélyezni.",

                modifier =
                    Modifier.padding(
                        top = 12.dp,
                        bottom = 24.dp
                    )
            )
        }
    }
}

@Composable
private fun SubscriptionPlanCard(
    plan: SubscriptionPlan,
    selectedPeriod: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val price =
        if (
            selectedPeriod ==
            SubscriptionPlans.PERIOD_WEEKLY
        ) {
            plan.weeklyPriceFt
        } else {
            plan.monthlyPriceFt
        }

    val periodText =
        if (
            selectedPeriod ==
            SubscriptionPlans.PERIOD_WEEKLY
        ) {
            "hét"
        } else {
            "hónap"
        }

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    18.dp
                )
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text =
                        plan.name
                )

                Text(
                    text =
                        "$price Ft / $periodText"
                )
            }

            Text(
                text =
                    "Csomag tag: ${plan.tag}",

                modifier =
                    Modifier.padding(
                        top = 10.dp
                    )
            )

            Text(
                text =
                    "+${SubscriptionPlans.SUBSCRIPTION_REPUTATION_REWARD} " +
                            "reputációs pont minden sikeresen feldolgozott előfizetéskor",

                modifier =
                    Modifier.padding(
                        top = 8.dp
                    )
            )

            Button(
                onClick =
                    onSelect,

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 16.dp
                    )
            ) {

                Text(
                    text =
                        if (selected) {
                            "✓ Kiválasztva"
                        } else {
                            "Csomag kiválasztása"
                        }
                )
            }
        }
    }
}