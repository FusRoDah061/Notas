/**
 * Classe criada em 22/10/2016 por Allex Rodrigues
 * 
 * 22/10/2016: 
 * 	- Criada;
 *  - Implementado o salvamento da nota pelo botão Salvar, usando SharedPreferences e atualizando diretamente o widget;
 *  - Implementada a checagem do conteúdo da nota no onCreate;
 *  
 * 23/10/2016:
 * 	- Adicionado o botão Limpar
 *  - Criados os métodos Salvar e Limpar;
 *  - Adicionado o salvamento no onStop;
 *  - Removido Toast de nota salva;
 *  - Adicionado TextWatcher;
 *  - Adicionado o evento de TextChange;
 *  - Implementado o comportamento de lista caso o usuário comece uma frase com -, #, * ou > (no onTextChanged);
 */

package es.esy.amaralallex.stickynotes;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MainActivity extends Activity implements TextWatcher {

	private EditText etNoteContent;
	private char caractereDeLista;
	private int currentLineCount = 1;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        etNoteContent = (EditText)findViewById(R.id.etNoteContent);
        etNoteContent.addTextChangedListener(this);
        
        SharedPreferences prefs = getSharedPreferences(Constantes.PREFS_NAME, Context.MODE_PRIVATE);
        String nota = prefs.getString(Constantes.NOTES_FIELD, "Sem notas.");
        
        //Caso o texto salvo nas preferências do app esteja vazio, ou seja, o texto
        //da TextView do widget esteja vazio, mostra a frase 'Sem notas.'
        if(!nota.equals("Sem notas.")){
        	etNoteContent.setText(nota);
        }
        else{
        	etNoteContent.setText("");
        }
        
        //Posiciona o cursor no final do texto
        etNoteContent.setSelection(etNoteContent.getText().toString().length());
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_salvar) {
        	//Salva as alterações
        	Salvar();

            return true;
        }
        if (id == R.id.action_limpar) {
        	//Salva as alterações
        	Limpar();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    //Salva o conteúdo digitado na EditText no arquivo de preferências e manda também pro widget
    private void Salvar(){
    	//Obtém o trexto digitado
    	String notaTexto = etNoteContent.getText().toString();
    	
    	//se estiver vazio atriubui uma frase padrão.
    	if(notaTexto.equals("")){
    		notaTexto = "Sem notas.";
    	}
    	
    	//Cria o objeto SharedPreferences
    	SharedPreferences prefs = getSharedPreferences(Constantes.PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		
		//Adiciona o conteúdo digitado ao SharedPreferences do aplicativo
		editor.putString(Constantes.NOTES_FIELD, notaTexto);
		
		//Checa se as alteraçõs foram salvas com sucesso ou não
		if(editor.commit()){
			
			Context context = this;
			//Obtém uma instância do gerenciador de widgets para essa aplicação
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			//RemoteViews é o cinjunto de Views presentes no widget
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_app_layout);
			//Obtém o widget
			ComponentName thisWidget = new ComponentName(context, StickyNoteWidget.class);
			//Atribui o texto digitado à TextView do widget
			remoteViews.setTextViewText(R.id.tvNoteContent, notaTexto);
			//Atualiza o widget
			appWidgetManager.updateAppWidget(thisWidget, remoteViews);
			
			//Fecha a tela
			finish();
		}
		else{
			Toast.makeText(this, "Não foi possível salvar a nota!", Toast.LENGTH_SHORT).show();
		}
    }
    
    //Limpa o texto na EditText
    private void Limpar(){
    	etNoteContent.setText("");
    }

    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    	//Salva as alterações após fechar essa tela
    	Salvar();
    }

    
    //Checa se um valor existe dentro de um vetor, ambos do tipo char.
    private boolean Contem(char[] colecao, char item){
    	
    	for(char car : colecao){
    		if(car == item){
    			//Armazena o item que contém no vetor para uso futuro.
    			caractereDeLista = car;
    			return true;
    		}
    	}
    	
    	return false;
    }
    
	@Override
	public void afterTextChanged(Editable s) {}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

		//Possibilidades de caracteres para ser considerada uma lista
		char[] possibilidades = {'-', '*', '>', '#'};
		
		//Quantidade de linha atualmente nea EditText
		int tmpLineCount = etNoteContent.getLineCount();
		
		//Texto da EditText
		String texto = s.toString();
	
		//Se a EditText não estiver vazia
		if(texto.length() > 0){
			//E conter uma das possibilidades de lista
			if(Contem(possibilidades, texto.charAt(0))){
				
				//Aqui é checado se a houve alteração na quantidade de linah da EditText.
				//currentLineCount representa a quantidade anterior de linhas, dessa forma, quando o usuário dar um
				//Enter a quantidade será alterada, tornando a condição verdadeira.
				if(tmpLineCount > currentLineCount){
					//Atualiza a quantidade de linhas
					currentLineCount = tmpLineCount;
					//Adiciona o caractere de lista no fim do texto (consequentemente após a quebra de linha)
					texto += caractereDeLista + " ";
					etNoteContent.setText(texto);
					//Posiciona o cursor no final do texto da EditText
					etNoteContent.setSelection(texto.length());
				}
				else{
					//Se este bloco for chamado significa que o usuário apagou uma linha do texto,
					//tornando neceário atualizá-lo para disparar o bloco if acima.
					currentLineCount = tmpLineCount;
				}
			}
		}

	}
}
