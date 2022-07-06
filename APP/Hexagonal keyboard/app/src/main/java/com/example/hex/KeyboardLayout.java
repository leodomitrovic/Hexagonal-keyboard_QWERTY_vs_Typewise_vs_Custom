package com.example.hex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;

import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.List;

// Layout tipkovnice -- izgled i ponasanje
@SuppressLint("ViewConstructor")
public class KeyboardLayout extends LinearLayout {

	Context context;
	LayoutInflater inflater;
	InputConnection inputConn;
	int currentIMEaction;

	boolean HAND;

	ArrayList<Button> buttonList; 	// kolekcija svih buttona
	LinearLayout rootLL;			// vrsni layout tipkovnice


	public KeyboardLayout(LayoutInflater inflater, Context ctx,
						  InputConnection inputConn,
						  int currentIMEaction,
						  Integer kbLayout, boolean hand) {

		super(ctx);
		this.context = ctx;
		this.inflater = inflater;
		this.inputConn = inputConn;
		this.currentIMEaction = currentIMEaction;

		this.HAND = hand;

		// Postavljanje/aktualiziranje dimenzija tipkovnice (primarno visine pomocu
		// zadanog "tezinskog faktora" u odnosnu na cijelu visinu zaslona):
		//float kbScale = KEYBOARD_HEIGHT_SCALE_FACTOR * dm.heightPixels;
		//int availableHeight = (int)kbScale;

		// Ucitavanje layout-a tipkovnice iz odgovarajuceg xml-a:
		int flag = kbLayout;    // 0 - qwerty,   1 - typewise,    2 - custom

		if (flag == 2) {
			if (HAND) {
				inflater.inflate(R.layout.custom, this);
			} else {
				inflater.inflate(R.layout.custom_left, this);
			}
		} else if (flag == 1) {
			if (HAND) {
				inflater.inflate(R.layout.typewise, this);
			} else {
				inflater.inflate(R.layout.typewise_left, this);
			}
		} else {
			inflater.inflate(R.layout.qwerty, this);
		}

		rootLL = this.findViewById(R.id.rootview);

		// Apliciranje visine tipkovnice promjenom parametra za vrsni UI-layout:
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rootLL.getLayoutParams();
		//params.height = availableHeight;
		rootLL.setLayoutParams(params);



		// probati sad računati širinu i visinu

		// Instanciranje svih buttona i generiranje odgovarajuce liste:
		create_button_list(flag);

		// Regitracija svih potrebnih listenera za sve buttone
		register_button_listeners(flag);
	}


	// Instanciranje svih button objekata i generiranje odgovarajuce kolekcije pomocu pretrage
	// hijerarhije UI elemenata:
	private void create_button_list(int flag){

		// Pronadji sve UI elemente u root view-u (to je vrsni LinearLayout u slucaju ove aplikacije):
		ConstraintLayout cl = findViewById(R.id.cl);
		List<View> allElements = getAllChildrenBFS(cl);
		//ConstraintSet cs = new ConstraintSet();
		//cs.clone(cl);
		List<ViewGroup.LayoutParams> pom1 = new ArrayList<>();

		DisplayMetrics dm = this.context.getResources().getDisplayMetrics();

		//Log.d("Test", "Sirina " + this.context.getResources());
		int density = dm.densityDpi;
		int width;
		width = dm.widthPixels;

		// Filtriranje buttona i dodavanje u odnosnu kolekciju:
		buttonList = new ArrayList<>();

		for (int i = 0; i < allElements.size(); i++) {
			final View iElement = allElements.get(i);
			if (iElement instanceof Button) {
				iElement.setPaddingRelative(0, 0, 0, 0);
				((Button)iElement).setTransformationMethod(null);
				iElement.setSoundEffectsEnabled(true);
				((Button)iElement).setText(((Button)iElement).getText().toString().toLowerCase());

				pom1.add(iElement.getLayoutParams());

				int settings = 0;

				if (flag == 0) {
					settings = R.id.button2;
				} else if (flag == 1) {
					settings = R.id.button78;
				}else {
					if (HAND) {
						settings = R.id.button84;
					} else {
						settings = R.id.button83;
					}
				}

				if (iElement.getId() == settings) {
					iElement.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View view) {
							context.startActivity(new Intent(context, ImePreferences.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
						}
					});
				}

				buttonList.add((Button)iElement);
			}
		}

		// Razmak između buttona i širina
		int empty;
		int size = 0;
		// constraints ukupno: 18 dp = 48.6 px

		Log.d("Width", Integer.toString(width));

		if (flag == 1) {
			empty = (12 * density) / 160;
			size = (width - 20 - empty) / 7;
		} else if (flag == 0) {
			empty = (18 * density) / 160;
			size = (width - 20 - empty) / 10;
		} else {
			empty = (14 * density) / 160;
			size = (width - 20 - empty) / 8;
		}

		for (int i = 0; i < buttonList.size(); i++) {
			ViewGroup.LayoutParams elem = pom1.get(i);
			elem.height = size;
			elem.width = size;
			buttonList.get(i).setLayoutParams(elem);
		}
	}


	// Pronadji sve View elemente u nekom root View elementu (UI hijerarhija);
	// (koristi se Breadth-First Search):
	private List<View> getAllChildrenBFS(View v) {
		List<View> visited = new ArrayList<>();
		List<View> unvisited = new ArrayList<>();
		unvisited.add(v);

		while (!unvisited.isEmpty()) {
			View child = unvisited.remove(0);
			visited.add(child);
			if (!(child instanceof ViewGroup)) continue;
			ViewGroup group = (ViewGroup) child;
			final int childCount = group.getChildCount();
			for (int i=0; i<childCount; i++) {
				unvisited.add(group.getChildAt(i));
			}
		}
		return visited;
	}


	// Registracija TOUCH i HOVER listenera za sve buttone u glavnoj kolekciji;
	// Omogucit ce se i klizanje po tipkovnici (dodirom), te "lebdenje" iznad buttona.
	// Pri tome, ucinak akcije "lebdenja" se moze testirati putem mousea (OTG support)
	@SuppressLint("ClickableViewAccessibility")
	private void register_button_listeners(int flag){

		for(Button iButton : buttonList){
			////// TOUCH listeneri:

			iButton.setOnTouchListener((v, event) -> {
				final int action = event.getAction();

				if (action == MotionEvent.ACTION_DOWN) {
					int insideIndex = checkInsideButton((int)event.getRawX(), (int)event.getRawY());
					if (insideIndex == -1) return false;
					Drawable d = getResources().getDrawable(R.drawable.ic_hexagon_pressed);
					buttonList.get(insideIndex).setBackground(d);
				}

				if (action == MotionEvent.ACTION_MOVE) {
					int insideIndex = checkInsideButton((int)event.getRawX(), (int)event.getRawY());
					if (insideIndex == -1) return false;
					Drawable d = getResources().getDrawable(R.drawable.ic_hexagon);
					buttonList.get(insideIndex).setBackground(d);
				}

				if (action == MotionEvent.ACTION_UP) {
					int insideIndex = checkInsideButton((int)event.getRawX(), (int)event.getRawY());
					if (insideIndex == -1) return false;
					Drawable d = getResources().getDrawable(R.drawable.ic_hexagon);
					buttonList.get(insideIndex).setBackground(d);
					Log.d("Index", Integer.toString(insideIndex));
					Log.d("Flag", Integer.toString(flag));
					Log.d("Hand", Boolean.toString(HAND));
					if (flag == 1) {
						if (HAND == true) {
							if (insideIndex == 28){ // DEL
								// brisanje:
								inputConn.deleteSurroundingText(1, 0);
							} else if (insideIndex == 27) { // SPACE  //12-qwerty  //25 i 27-typewise
								// prazan (blank) znak:
								inputConn.commitText(" ", 1);
							} else if (insideIndex == 14) { // ENTER
								// ili prelazak u novi redak ili odgovarajuca akcija editora:
								if (KeyboardLayout.this.currentIMEaction ==
										EditorInfo.IME_ACTION_UNSPECIFIED) {
									inputConn.commitText("\n", 1);
								} else {
									inputConn.performEditorAction(currentIMEaction);
								}
							} else if (insideIndex != -1) { // REGULAR CHAR
								// slovo:
								Button target = buttonList.get(insideIndex);
								inputConn.commitText(target.getText(), 1);
							}
						} else {
							if (insideIndex == 12){ // DEL
								// brisanje:
								inputConn.deleteSurroundingText(1, 0);
							} else if (insideIndex == 25) { // SPACE  //12-qwerty  //25 i 27-typewise
								// prazan (blank) znak:
								inputConn.commitText(" ", 1);
							} else if (insideIndex == 14) { // ENTER
								// ili prelazak u novi redak ili odgovarajuca akcija editora:
								if (KeyboardLayout.this.currentIMEaction ==
										EditorInfo.IME_ACTION_UNSPECIFIED) {
									inputConn.commitText("\n", 1);
								} else {
									inputConn.performEditorAction(currentIMEaction);
								}
							} else if (insideIndex != -1) { // REGULAR CHAR
								// slovo:
								Button target = buttonList.get(insideIndex);
								inputConn.commitText(target.getText(), 1);
							}
						}
					} else if (flag == 0) {
						if (insideIndex == 2){ // DEL
							// brisanje:
							inputConn.deleteSurroundingText(1, 0);
						} else if (insideIndex == 13) { // SPACE
							// prazan (blank) znak:
							inputConn.commitText(" ", 1);
						} else if (insideIndex == 14) { // ENTER
							// ili prelazak u novi redak ili odgovarajuca akcija editora:
							if (KeyboardLayout.this.currentIMEaction ==
									EditorInfo.IME_ACTION_UNSPECIFIED) {
								inputConn.commitText("\n", 1);
							} else {
								inputConn.performEditorAction(currentIMEaction);
							}
						} else if (insideIndex != -1) { // REGULAR CHAR
							// slovo:
							Button target = buttonList.get(insideIndex);
							inputConn.commitText(target.getText(), 1);
						}
					} else if (flag == 2) {
						//Log.d("Index", Integer.toString(insideIndex));
						if (HAND == true) {
							if (insideIndex == 21){ // DEL
								// brisanje:
								inputConn.deleteSurroundingText(1, 0);
							} else if (insideIndex == 15) { // SPACE
								// prazan (blank) znak:
								inputConn.commitText(" ", 1);
							} else if (insideIndex == 17) { // ENTER
								// ili prelazak u novi redak ili odgovarajuca akcija editora:
								if (KeyboardLayout.this.currentIMEaction ==
										EditorInfo.IME_ACTION_UNSPECIFIED) {
									inputConn.commitText("\n", 1);
								} else {
									inputConn.performEditorAction(currentIMEaction);
								}
							} else if (insideIndex != -1) { // REGULAR CHAR
								// slovo:
								Button target = buttonList.get(insideIndex);
								inputConn.commitText(target.getText(), 1);
							}
						} else {
							if (insideIndex == 24){ // DEL
								// brisanje:
								inputConn.deleteSurroundingText(1, 0);
							} else if (insideIndex == 13) { // SPACE
								// prazan (blank) znak:
								inputConn.commitText(" ", 1);
							} else if (insideIndex == 11) { // ENTER
								// ili prelazak u novi redak ili odgovarajuca akcija editora:
								if (KeyboardLayout.this.currentIMEaction ==
										EditorInfo.IME_ACTION_UNSPECIFIED) {
									inputConn.commitText("\n", 1);
								} else {
									inputConn.performEditorAction(currentIMEaction);
								}
							} else if (insideIndex != -1) { // REGULAR CHAR
								// slovo:
								Button target = buttonList.get(insideIndex);
								inputConn.commitText(target.getText(), 1);
							}
						}
					}
					/*else if (insideIndex == 29) { // SETINGS
						// Eksplicitno pozivanje settings-a,
						// preporucljivo implementirati kada se radi IME!
						Intent mIntent = new Intent(context, com.example.myapplication.ImePreferences.class);
						mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						KeyboardLayout.this.context.startActivity(mIntent);
					}*/

					// Zvucni feedback
					if (insideIndex != -1){
						v.performClick();
						v.playSoundEffect(SoundEffectConstants.CLICK);
					}
				}
				return false;
			});
		}
	}


	// Provjera: nalazi li se tocka trenutnog dodira (na zaslonu) unutar nekog od buttona na tipkovnici?
	// Ulaz: kordinata dodira (pointerX, pointerY)
	// Izlaz: indeks buttona u glavnoj kolekciji (ako sadrzi tu tocku), -1 ako takav button ne postoji
	private int checkInsideButton(int pointerX, int pointerY){
		String data[][] = new String[buttonList.size()][4];
		int i = 0;
		for (Button iButton : buttonList) {
			int[] loc = new int[2];
			iButton.getLocationOnScreen(loc);
			int w = iButton.getWidth();
			int h = iButton.getHeight();
			data[i][0] = Integer.toString(loc[0] + (w / 2));
			data[i][1] = Integer.toString(loc[1] + (h / 2));
			data[i][2] = Integer.toString(w);
			data[i][3] = Integer.toString(h);
			//Log.d("Buttons", iButton.getText() + " " + data[i][0] + " " + data[i][1] + " " + data[i][2] + " " + data[i][3]);
			//count++;
			if (isInside(pointerX, pointerY,
					loc[0], loc[1],
					loc[0] + iButton.getWidth(),
					loc[1] + iButton.getHeight())){
				//Log.d("x i y", "X: " + pointerX + " Y: " + pointerY);
				return buttonList.indexOf(iButton);
			}
		}
		return -1;
	}

	double sign(double p1[], double p2[], double p3[])
	{
		return (p1[0] - p3[0]) * (p2[1] - p3[1]) - (p2[0] - p3[0]) * (p1[1] - p3[1]);
	}

	boolean PointInTriangle(double pt[], double v1[], double v2[], double v3[])
	{
		double d1, d2, d3;
		boolean has_neg, has_pos;

		d1 = sign(pt, v1, v2);
		d2 = sign(pt, v2, v3);
		d3 = sign(pt, v3, v1);

		has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
		has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);

		return !(has_neg && has_pos);
	}


	// Pomocna metoda za provjeru polozaja tocke dodira u geometriji buttona;
	// konkretno -> nalazi li se tocka (x, y) u pravokutniku (left, top, right, bottom):
	private boolean isInside(int pointerX, int pointerY,
							 int boundLeft, int boundTop, int boundRight, int boundBottom){

		int bounds = boundBottom - boundTop;
		//boolean inside = false;
		if ((pointerX >= boundLeft) && (pointerX <= boundRight) &&
				(pointerY >= boundTop) && (pointerY <= boundBottom)) {
			double a = bounds / (Math.sqrt(3));
			double slobodno = bounds - a;
			double jedna_slobodna_strana = slobodno / 2;
			// duljina stranice = 84.293
			// ukupno slobodno = 61.706  ->  jedna strana = 30.853
			if ((pointerY >= boundTop + jedna_slobodna_strana && pointerY <= boundBottom - jedna_slobodna_strana) || (pointerX == boundLeft + (bounds / 2))) return true;
			else {

				double iznad = boundTop + jedna_slobodna_strana;
				double ispod = boundBottom - jedna_slobodna_strana;
				if (PointInTriangle(new double[]{pointerX, pointerY}, new double[]{boundLeft, iznad}, new double[]{boundLeft + (bounds / 2), boundTop}, new double[]{boundRight, iznad}) ||
					PointInTriangle(new double[]{pointerX, pointerY}, new double[]{boundLeft, ispod}, new double[]{boundLeft + (bounds / 2), boundBottom}, new double[]{boundRight, ispod})) {
					return true;
				}
				/*int i = 0;
				while (i < boundTop) {
					//ne radi dolje lijevo
					if ((pointerX >= boundLeft + i && pointerY >= iznad - i) && (pointerX <= boundRight - i && pointerY >= iznad - i) &&
							(pointerX >= boundLeft + i && pointerY <= ispod + i) && (pointerX >= boundRight - i && pointerY <= ispod + i)) {
						inside = true;
						break;
					}
					i++;
				}
				//Log.d("Granice", iznad + " " + (iznad - a + 1) + " " + boundTop);
				if (inside) return true;*/
			}
		}
		return false;
	}


	// Azuriranje InputConnection objekta ("exposano" za glavni servis [AirViewIME])
	public void setInputConnection(InputConnection ic){
		this.inputConn = ic;
	}


	// Azuriranje IME action-a ("exposano" za glavni servis [AirViewIME])
	public void setIMEaction(int imeAction){
		this.currentIMEaction = imeAction;
	}


}