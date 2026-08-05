package com.example.dilo.DiloBackend.service.implementation;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class NlpService {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "por", "favor", "porfavor", "porfa", "gracias", "hola", "buenos", "dias", "tardes", "noches",
            "ia", "sistema", "asistente", "dilo", "ok", "hey", "buenas", "ayudame",

            "amor", "querida", "querido", "pana", "ñaño", "ñaña", "veci", "casero", "caserita", "caserito",
            "jefe", "jefecito", "bro", "brother", "amigo", "amiga", "rey", "reina", "corazon", "lindo",
            "linda", "estimado", "estimada", "compadre", "socio", "pues", "nomas", "mijito", "mijo",

            "el", "la", "los", "las", "un", "una", "unos", "unas", "este", "esta", "estos", "estas",
            "ese", "esa", "esos", "esas", "mi", "mis", "tu", "tus", "su", "sus", "me", "te", "se", "nos",

            "a", "ante", "bajo", "con", "contra", "de", "desde", "en", "entre", "hacia", "hasta",
            "para", "segun", "sin", "sobre", "tras", "y", "e", "o", "u", "que", "al", "del", "como", "muy",
            "producto", "cliente", "articulo", "item", "persona", "señor", "senor", "señora", "senora"
    ));

    private static final String VERBOS_REGEX = "\\b(busc[a-z]*|quier[a-z]*|quisier[a-z]*|necesit[a-z]*|agreg[a-z]*|factur[a-z]*|pon[a-z]*|vend[a-z]*|trae[a-z]*|muestr[a-z]*|encuentr[a-z]*|anad[a-z]*|añad[a-z]*|dame[a-z]*)\\b";

    public String limpiarComandoVoz(String textoVoz) {
        if (textoVoz == null || textoVoz.trim().isEmpty()) {
            return "";
        }

        String textoNormalizado = Normalizer.normalize(textoVoz, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();

        textoNormalizado = textoNormalizado.replaceAll("[^a-z0-9\\s]", "");

        textoNormalizado = textoNormalizado.replaceAll(VERBOS_REGEX, "");

        String[] palabras = textoNormalizado.split("\\s+");
        StringBuilder entidadExtraida = new StringBuilder();

        for (String palabra : palabras) {
            if (!palabra.isEmpty() && !STOP_WORDS.contains(palabra)) {
                if (palabra.length() > 1 || palabra.matches("[0-9]")) {
                    entidadExtraida.append(palabra).append(" ");
                }
            }
        }

        return entidadExtraida.toString().trim();
    }
}