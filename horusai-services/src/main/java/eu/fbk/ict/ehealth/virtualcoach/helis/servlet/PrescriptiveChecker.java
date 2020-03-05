package eu.fbk.ict.ehealth.virtualcoach.helis.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class PrescriptiveChecker
 */
public class PrescriptiveChecker extends HttpServlet {
  private static final long serialVersionUID = 1L;

  /**
   * @see HttpServlet#HttpServlet()
   */
  public PrescriptiveChecker() {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // TODO Auto-generated method stub
    response.getWriter().append("Served at: ").append(request.getContextPath());
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // TODO Auto-generated method stub
    doGet(request, response);
  }

  
  
  
  private void dietChecker() {
    
    HashMap<String, ArrayList<String>> feedback = new HashMap<String, ArrayList<String>>();
    
    ArrayList<String> messages = null;
    
    // CIRFOOD-808_Pasta aglio olio e peperoncino
    messages = new ArrayList<String>();
    messages.add("I grassi polinsaturi, come gli acidi omega 6, contenuti nell'olio extravergine d'oliva, aiutano a ridurre i livelli di colesterolo nel sangue.");
    feedback.put("CIRFOOD-808", messages);
    
    // CIRFOOD-809_Crema di cavolfiore
    messages = new ArrayList<String>();
    messages.add("Ben fatto! Hai consumato la giusta porzione giornaliera di verdura.");
    feedback.put("CIRFOOD-809", messages);
    
    // CIRFOOD-812_Patate al forno
    messages = new ArrayList<String>();
    messages.add("Le patate non sono verdure ma un alimento ricco di carboidrati. Cerca di limitarne il consumo settimanale.");
    feedback.put("CIRFOOD-812", messages);
    
    // CIRFOOD-814_Trancio di salmone ai ferri
    messages = new ArrayList<String>();
    messages.add("Il pesce è un cibo ricco di acidi omega 3, elementi fondamentali per il benessere del nostro organismo.");
    feedback.put("CIRFOOD-814", messages);
    
    // CIRFOOD-817_Carote al vapore
    messages = new ArrayList<String>();
    messages.add("Ben fatto! Hai consumato la giusta porzione giornaliera di verdura.");
    feedback.put("CIRFOOD-817", messages);
    
    // CIRFOOD-819_Polenta
    messages = new ArrayList<String>();
    messages.add("I cereali integrali sono alleati preziosi nella prevenzione di alcuni disturbi dell’apparato cardiocircolatorio e dell’obesità.");
    feedback.put("CIRFOOD-819", messages);
    
    // CIRFOOD-821_Pasta al ragù
    messages = new ArrayList<String>();
    messages.add("La pasta al ragu' contiene carne. Cerca di limitarne il consumo a non piu' di due volte a settimana.");
    feedback.put("CIRFOOD-821", messages);
    
    // CIRFOOD-823_Pasta pomodoro e ricotta
    messages = new ArrayList<String>();
    messages.add("Prova a variare la qualità dei cereali da consumare: scegli l'orzo, il farro, la quinoa e l'amaranto.");
    feedback.put("CIRFOOD-823", messages);
    
    // CIRFOOD-825_Strudel di verdura
    messages = new ArrayList<String>();
    messages.add("Ben fatto! Hai consumato la giusta porzione giornaliera di verdura.");
    feedback.put("CIRFOOD-825", messages);
    
    // CIRFOOD-828_Pasta all'arrabbiata
    messages = new ArrayList<String>();
    messages.add("Limita il consumo di pasta troppo elaborata. Scegli di accompagnare la pasta e altri cereali con salse semplici e con verdure.");
    feedback.put("CIRFOOD-828", messages);
    
    // CIRFOOD-829_Hamburger di ceci
    messages = new ArrayList<String>();
    messages.add("Bene! I legumi sono alimenti ricchi di fibre che aiutano a regolarizzare l'intestino.");
    feedback.put("CIRFOOD-829", messages);
    
    // CIRFOOD-830_Crema di carote
    messages = new ArrayList<String>();
    messages.add("Ben fatto! Hai consumato la giusta porzione giornaliera di verdura.");
    feedback.put("CIRFOOD-830", messages);
    
    // CIRFOOD-832_Gnocchi di patate pomodoro e basilico
    messages = new ArrayList<String>();
    messages.add("Le patate non sono verdure ma un alimento ricco di carboidrati. Cerca di limitarne il consumo settimanale.");
    feedback.put("CIRFOOD-832", messages);
    
    // CIRFOOD-833_Passato di verdura
    messages = new ArrayList<String>();
    messages.add("Ben fatto! Hai consumato la giusta porzione giornaliera di verdura.");
    feedback.put("CIRFOOD-833", messages);
    
    // CIRFOOD-834_Orata al forno
    messages = new ArrayList<String>();
    messages.add("Il pesce azzurro è ricco di vitamina A che protegge le cellule dai radicali liberi e aiuta l'organismo ad aumentare le difese immunitarie.");
    feedback.put("CIRFOOD-834", messages);
    
    // CIRFOOD-843_PurК di patate
    messages = new ArrayList<String>();
    messages.add("Le patate non sono verdure ma un alimento ricco di carboidrati. Cerca di limitarne il consumo settimanale.");
    feedback.put("CIRFOOD-843", messages);
    
    // CIRFOOD-846_Pasta alla carbonara
    messages = new ArrayList<String>();
    messages.add("Limita il consumo di pasta troppo elaborata. Scegli di accompagnare la pasta e altri cereali con salse semplici e con verdure.");
    feedback.put("CIRFOOD-846", messages);
    
    // CIRFOOD-849_Pasta al pomodoro
    messages = new ArrayList<String>();
    messages.add("Ricorda che puoi alternare il consumo di cereali con quello di patate, limitandone il consumo a due porzioni settimanli.");
    feedback.put("CIRFOOD-849", messages);
    
    // CIRFOOD-853_Petto di pollo alla valdostana
    messages = new ArrayList<String>();
    messages.add("Il consumo eccessivo di proteine di orgini animale può aumentare il rischio di aterosclerosi, tumori, osteoporosi, ipertensione, calcolosi urinaria perchè contenute in alimenti ricchi anche di grassi e colesterolo, come la carne rossa.");
    feedback.put("CIRFOOD-853", messages);
    
    // CIRFOOD-861_Cous cous di pesce
    messages = new ArrayList<String>();
    messages.add("I cereali, specie se integrali, soo ricchi di fibre che aiutano a raggiungere facilmente la sensazione di sazietà.");
    feedback.put("CIRFOOD-861", messages);
    
    // CIRFOOD-865_Polpettone di verdura
    messages = new ArrayList<String>();
    messages.add("Ben fatto! Hai consumato la giusta porzione giornaliera di verdura.");
    feedback.put("CIRFOOD-865", messages);
    
    // CIRFOOD-887_Risotto con zucchine e zafferano
    messages = new ArrayList<String>();
    messages.add("Prova a variare la qualità dei cereali da consumare: scegli l'orzo, il farro, la quinoa e l'amaranto.");
    feedback.put("CIRFOOD-887", messages);
    
    // CIRFOOD-892_Formaggi
    messages = new ArrayList<String>();
    messages.add("I formaggi sono cibi proteici e rappresentano una buona alternativa alla carne, ma limitane il numero di porzioni a quelle suggerite dalla Dieta Mediterranea.");
    feedback.put("CIRFOOD-892", messages);
    
    // CIRFOOD-913_Risotto ai funghi
    messages = new ArrayList<String>();
    messages.add("Prova a variare la qualità dei cereali da consumare: scegli l'orzo, il farro, la quinoa e l'amaranto.");
    feedback.put("CIRFOOD-913", messages);
    
    // CIRFOOD-937_Spinaci al vapore
    messages = new ArrayList<String>();
    messages.add("Ben fatto! Hai consumato la giusta porzione giornaliera di verdura.");
    feedback.put("CIRFOOD-937", messages);
    
    // CIRFOOD-959_Brasato al vino rosso con patate
    messages = new ArrayList<String>();
    messages.add("Il consumo eccessivo di proteine di orgini animale può aumentare il rischio di aterosclerosi, tumori, osteoporosi, ipertensione, calcolosi urinaria perchè contenute in alimenti ricchi anche di grassi e colesterolo, come la carne rossa.");
    feedback.put("CIRFOOD-959", messages);
    
    // FOOD-2019_Torta Margherita
    messages = new ArrayList<String>();
    messages.add("I dolci sono cibi che contengono grassi saturi oltre agli zuccheri. Consumarne aumenta il rischio di obesità.");
    feedback.put("FOOD-2019", messages);
    
    // FOOD-9001_Insalata
    messages = new ArrayList<String>();
    messages.add("Ben fatto! Hai consumato la giusta porzione giornaliera di verdura.");
    feedback.put("FOOD-9001", messages);
    
    // FOOD-100230_Torta al Cioccolato
    messages = new ArrayList<String>();
    messages.add("I dolci sono cibi che contengono grassi saturi oltre agli zuccheri. Consumarne aumenta il rischio di obesità.");
    feedback.put("FOOD-100230", messages);
    
    // RECIPE-11724_Grigliata Mista Di Carne
    messages = new ArrayList<String>();
    messages.add("La carne rossa è ricca di grassi animali; un consumo esagerato può contribuire all'incremento del rischio di malattie cardiovascolari.");
    feedback.put("RECIPE-11724", messages);
    
    // RECIPE-11766_Gulasch Di Manzo
    messages = new ArrayList<String>();
    messages.add("Alterna le proteine animali con quelle vegetali, preferendo altri cibi proteici come i legumi.");
    feedback.put("RECIPE-11766", messages);
    
    // RECIPE-11780_Hamburger
    messages = new ArrayList<String>();
    messages.add("Prova a variare il tuo consumo di carne  alternandola periodicamente con uova o formaggi.");
    feedback.put("RECIPE-11780", messages);
    
    // TURCONI-43-85_Bistecca di manzo
    messages = new ArrayList<String>();
    messages.add("Limita il consumo di carni rosse, se possibile al massimo due volte a settimana, e preferisci le carni magre.");
    feedback.put("TURCONI-43-85", messages);
    
    // TURCONI-74-150_Bastoncini di pesce
    messages = new ArrayList<String>();
    messages.add("Il pesce è importante anche per la salute cardiovascolare. Un’alimentazione che non trascuri questo cibo può far allontanare il rischio di essere soggetti ad un attacco di cuore mortale.");
    feedback.put("TURCONI-74-150", messages);
    
    // TURCONI-79-150_Insalata di mare
    messages = new ArrayList<String>();
    messages.add("Non rinunciare a consumare pesce. Il pesce è ricco di sali minerali, come calcio, fosforo e iodio.");
    feedback.put("TURCONI-79-150", messages);
    
    // TURCONI-217-115_Banana
    messages = new ArrayList<String>();
    messages.add("Ben fatto! Hai consumato la giusta porzione giornaliera di frutta.");
    feedback.put("TURCONI-217-115", messages);
    
    // TURCONI-220-160_Mela
    messages = new ArrayList<String>();
    messages.add("Ben fatto! Hai consumato la giusta porzione giornaliera di frutta.");
    feedback.put("TURCONI-220-160", messages);
    
    // TURCONI-231-200_Macedonia di frutta
    messages = new ArrayList<String>();
    messages.add("Ben fatto! Hai consumato la giusta porzione giornaliera di frutta.");
    feedback.put("TURCONI-231-200", messages);
    
    // TURCONI-332-100_Fetta di Strudel di mele
    messages = new ArrayList<String>();
    messages.add("I dolci sono cibi che contengono grassi saturi oltre agli zuccheri. Consumarne aumenta il rischio di obesità.");
    feedback.put("TURCONI-332-100", messages);
    
  }
  
}
