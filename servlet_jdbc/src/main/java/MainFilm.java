import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.cj.protocol.Resultset;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class MainFilm extends HttpServlet {

    private static final String _URLCONNECITON = "jdbc:mysql://localhost:3306/filmsESeriesDb";

    private static final String _PASSWORD = "123456";
    private static final String _USER = "root";

    private static final String ACTION = "action";

    Connection connection;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            connection = DriverManager.getConnection(_URLCONNECITON, _USER, _PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        StringBuilder sb = new StringBuilder();
        Statement st;
        sb.append("<html><body>");
        try {
            st = connection.createStatement();
            ResultSet res = st.executeQuery("Select titolo,regista from flist order by RAND() limit 1");
            if (res.next()) {
                sb.append("<div><h1>Film consigliato</h1>").append("<br>").append("<p> Titolo : <b> ")
                        .append(res.getString("titolo")).append("</b><p>").append("<p> Regista : <b> ")
                        .append(res.getString("regista")).append("</b><p>");
            } else {
                sb.append("<div><h1>NON HO TROVATO NESSUN RISULTATO</h1>").append("<br>");
            }
            sb.append("</div>").append("<form action=\"/film\" method=\"post\">")
                    .append("<label for=\"titolo\" value=\"Titolo\">")
                    .append("<input type=\"text\" name=\"titolo\" >")
                    .append("<input type=\"submit\" name=\"action\" value=\"Ricerca\">")
                    .append("</form>");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sb.append("</body></html>");
        resp.setContentType("text/html");
        PrintWriter printWriter = new PrintWriter(resp.getWriter());
        printWriter.write(sb.toString());
        printWriter.close();

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PreparedStatement st;
        StringBuilder sb = new StringBuilder();
        StringBuilder films = new StringBuilder();

        sb.append("<html><body>");
        try {
            switch (req.getParameter(ACTION).toLowerCase()) {
                case "ricerca":
                    st = connection.prepareStatement(
                            "Select titolo,regista from flist where titolo like ? or regista like ?",
                            ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    st.setString(1, "%" + req.getParameter("titolo") + "%");
                    st.setString(2, "%" + req.getParameter("titolo") + "%");
                    ResultSet resultset = st.executeQuery();
                    boolean haveResul = false;
                    films.append("<table><tr><th>Titolo</th><th>Regista</th><th><p></p></th><th><p></p></th></tr>");
                    while (resultset.next()) {
                        haveResul = true;
                        films.append("<tr>").append("<form action=\"/film\" method=\"post\">")
                                .append("<td><input type=\"text\" name=\"titolo\" readonly value=\" ")
                                .append(resultset.getString("titolo")).append("\"></td>")
                                .append("<td><input type=\"text\" name=\"regista\"readonly value=\" ")
                                .append(resultset.getString("regista")).append("\"></td>")
                                .append("<td><input type=\"submit\" name=\"action\" value=\"Modifica\"></td>")
                                .append("<td><input type=\"submit\" name=\"action\" value=\"Elimina\"></td>")
                                .append("</form></tr>");
                    }
                   
                    if (haveResul)
                        sb.append("</table").append(films);
                    else
                        sb.append("<p>Nessun risulttato trovato<p>");

                    sb.append("<form action=\"/film\" method=\"get\">")
                           .append("<input type=\"submit\" value=\"Torna indietro\"></form>");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        sb.append("</body></html>");
        resp.setContentType("text/html");
        PrintWriter printWriter = new PrintWriter(resp.getWriter());
        printWriter.write(sb.toString());
        printWriter.close();
    }

}