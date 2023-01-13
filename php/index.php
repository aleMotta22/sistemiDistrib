<?php

$server = "localhost";
$root = "root";
$pass = "123456";
$db = "filmseseriesdb";

$l = new mysqli($server, $root, $pass, $db);
echo "<html>";

if ($l->connect_error) {
    echo "errore connessione";
}

if ($_SERVER["REQUEST_METHOD"] === "GET") {

    $sql = "SELECT titolo,regista from flist order by RAND()";
    $res = $l->query($sql);
    if ($res->num_rows) {
        echo "<h1>Film consigliato </h1>";
        if ($row = $res->fetch_assoc()) {
            echo "<p> Titolo : <b>" . $row["titolo"] . " </b></p>";
            echo "<p> Regista : <b> " . $row["regista"] . "</b></p>";
            echo "<br>";
        }
        echo "<form method='post' action='/'><input type='text' name='text' >";
        echo "<input type='submit' name='search' value = 'Cerca'></form>";
    }
} else if ($_SERVER["REQUEST_METHOD"] === "POST") {
    if (isset($_POST["search"])) {
        $sql = "SELECT titolo,regista from flist where titolo like ? or regista like ?";
        $stmt = $l->prepare($sql);
        $param = "%" . $_POST['text'] . "%";
        $stmt->bind_param("ss", $param, $param);
        $stmt->execute();
        $res = $stmt->get_result();
        if ($res->num_rows) {
            echo "<table> <tr><th>Titolo</th><th>Regista</th><tr>";
            while ($row = $res->fetch_assoc()) {
                echo "<tr><form method='post' action='/'>";
                echo "<td><input type='text' readonly name='titolo' value=\"" . $row["titolo"] . "\"></td>
                    <td><input type='text' readonly name='regista' value=\"" . $row["regista"] . "\"></td>
                    <td><input type='hidden' name='oldTitolo' value=\"" . $row["titolo"] . "\"></td>
                    <td><input type='submit' name='prepareModifica' value='Modifica'></td>
                    <td><input type='submit' name='elimina' value='Elimina'></td>";
                echo "</form></tr>";
            }
            echo "</table>";
        } else
            echo "<h2>Nessun risultato trovato</h2>";
    } else if (isset($_POST["prepareModifica"])) {
        echo "<table> <tr><th>Titolo</th><th>Regista</th><tr>";
        echo "<tr><form method='post' action='/'>";
        echo "<td><input type='text' name='titolo' value=\"" . $_POST["titolo"] . "\"></td>
                <td><input type='text' name='regista' value=\"" . $_POST["regista"] . "\"></td>
                <td><input type='hidden' name='oldTitolo' value=\"" . $_POST["titolo"] . "\"></td>
                <td><input type='submit' name='modifica' value='Modifica'></td>
                <td><input type='submit' name='elimina' value='Elimina'></td>";
        echo "</form></tr></table>";
    } else if (isset($_POST["modifica"])) {
        $sql = "UPDATE flist set titolo = ?, regista = ? where titolo = ?";
        $smt = $l->prepare($sql);
        $smt->bind_param("sss", $_POST["titolo"], $_POST["regista"], $_POST["oldTitolo"]);
        if ($smt->execute()) {
            echo "<h2> La modifica effettuata con successo </h2>";
        } else {
            echo "<h2> Si è verificato un errore durante la modifica</h2>";
        }
    }
    else if (isset($_POST["elimina"])) {
        $sql = "DELETE FROM flist where titolo = ?";
        $smt = $l->prepare($sql);
        $smt->bind_param("s",$_POST["oldTitolo"]);
        $smt->execute();
        if ($smt->affected_rows > 0) {
            echo "<h2> Il film è stato eliminato successo </h2>";
        } else {
            echo "<h2> Si è verificato un errore durante l'eliminazione</h2>";
        }
    }

    echo "<br><form action='\' method='get'> <input type='submit' value ='Torna alla home'></form>";
}

echo "<html>";
