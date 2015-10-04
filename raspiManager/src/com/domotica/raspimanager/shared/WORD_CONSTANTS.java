package com.domotica.raspimanager.shared;

public interface WORD_CONSTANTS {
	String giusto="^[Ss]i.*|" +
			".*[Ee]satt.*|" +
			".*[Pp]erfett.*|" +
			".*[Gg]iusto.*|" +
			".*[Cc]orrett.*|" +
			".*[Rr]agion.*|" +
			".*[Bb]rav.*|" +
			".*[Ff]inalment.*";
	final static String[] rispostaOffesa = new String[]{" Grazie per il complimento, certe volte mi sento di troppo, ", " Stai diventando cattivo, o e'  impressione mia ? ", " Non si gioca con i sentimenti degli altri"," Posso non rispondere ? "};
	String sbagliato="^[Nn]o.*|" +
			".*[Nn]eanch.*|" +
			".*[Ss]bagli.*|" +
			".*[Nn]on.*|";
	
	String name=".*[Gg]iord.*|" +
			".*[Jj]arv.*|" +
			".*[Gg]iard.*|" +
			".*[Gg]iar.*|" +
			".*[Gg]ior.*|" ;
	
	String movimento="^.*[Mm]ovimen.*|" +
			"^.*[Mm]os.*|"+
			"^.*[Ff]amm.*|"+
			"^.*[Vv]ed.*|"+
			"^.*[Cc]ame.*";
	
	String on=".*^[Aa]ccend.*|" +
			".*^[Aa]ttiv.*|"+
			".*^[Aa]pe.*|"+
			".*^[Aa]pr.*";
	
	String casa=".*[Cc]asa.*|" +
			"^[Aa]ppartamento.*";
	
	String off=".*^[Ss]pegn.*|" +
			".*^[Dd]isattiv.*|" +
			".*^[Cc]hiud.*";
	
	String offesa=".*^[Ss]ei.*|" +
			".*^[Cc]ret.*|" +
			".*^[Ss]cem.*|" +
			".*^[Ss]tup.*|" +
			".*^[Vv]igliacco.*|" +
			".*^[Cc]acca.*|" +
			".*^embri^.*|" +
			".*^[Vv]atten.*|" +
			".*vai.*";
	
	
	String inizio="^[Hh]o [Dd]ett.*|";
	
	String inizio1="^[Tt]i [Hh]o [Dd]ett.*|"+
				"^[Tt]i [Ss]to [Dd]ic.*|";
	
	String orario=".*[Oo]ra.*|"+
			".*[Oo]rario.*|";
	
	String cerca=".*[Cc]erca.*|" +
			".*[Rr]icerca.*|" +
			".*[Tt]rova.*|";
	
	String sentimento = ".*[Cc]ome.*|";
	
	String ciao = ".*[Cc]iao.*|" +
				  ".*[Cc]ia.*|";
	
	String cerca1 = "^[Cc]erca [Ss]u [Ii]nter.*|"+
				    "^[Cc]erca [Ss]u [Cc]ro.*|";
	
	String allarme = ".*[Aa]llarm.*|" +
					 ".*[Pp]erimet.*|" +
					 ".*[Pr]otezione.*";
	
	String uscita = ".*[Uu]scend.*|" +
			 ".*[Uu]scir.*|" +
			 ".*[Pp]art.*|" +
			 ".*[Aa]nda.* .*[Vv]ia.*|";
	
	String entrata = ".*[En]tra.*|" +
			 ".*[Tt]orn.*|" +
			 ".*[Vv]ene.*|" +
			 ".*[Aa]rriv.*|";
	
	String garage = ".*[Gg]arage.*|" +
			 ".*[Ss]erran.^";
	
	String ritornello = ".*[Ss]opra [Ll]a [Pp]anc.*|";
}
