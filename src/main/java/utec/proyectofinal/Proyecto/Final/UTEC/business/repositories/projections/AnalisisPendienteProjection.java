package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.projections;


public interface AnalisisPendienteProjection {
    Long getLoteID();
    String getNomLote();
    String getFicha();
    String getEspecieNombre();
    String getCultivarNombre();
    String getTipoAnalisis(); // String porque viene de la query SQL
}
