package cl.duoc.cappucare.movimientos.client;

import cl.duoc.cappucare.movimientos.dto.AjusteStock;
import cl.duoc.cappucare.movimientos.dto.ProductoRemoto;
import cl.duoc.cappucare.movimientos.exception.ConflictoException;
import cl.duoc.cappucare.movimientos.exception.RecursoNoEncontradoException;
import cl.duoc.cappucare.movimientos.exception.ServicioNoDisponibleException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class ProductosRestClient implements ProductosClient {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final RestClient http;

    public ProductosRestClient(RestClient.Builder constructor, @Value("${productos.service.url}") String urlBase) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(3_000);
        fabrica.setReadTimeout(5_000);
        this.http = constructor.baseUrl(urlBase).requestFactory(fabrica).build();
    }

    @Override
    public ProductoRemoto obtener(String codigo) {
        try {
            return http.get().uri("/api/productos/{codigo}", codigo).retrieve().body(ProductoRemoto.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw noExiste(codigo);
        } catch (ResourceAccessException e) {
            throw noDisponible();
        }
    }

    @Override
    public ProductoRemoto ajustarStock(String codigo, int delta) {
        try {
            return http.post()
                    .uri("/api/productos/{codigo}/ajustar-stock", codigo)
                    .body(new AjusteStock(delta))
                    .retrieve()
                    .body(ProductoRemoto.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw noExiste(codigo);
        } catch (HttpClientErrorException.Conflict e) {
            throw new ConflictoException(extraerMensaje(e));
        } catch (ResourceAccessException e) {
            throw noDisponible();
        }
    }

    private RecursoNoEncontradoException noExiste(String codigo) {
        return new RecursoNoEncontradoException("No existe un insumo con el código " + codigo);
    }

    private ServicioNoDisponibleException noDisponible() {
        return new ServicioNoDisponibleException("El servicio de insumos no está disponible en este momento");
    }

    private String extraerMensaje(HttpClientErrorException e) {
        try {
            return JSON.readTree(e.getResponseBodyAsString()).path("mensaje").asText("Conflicto al ajustar el stock");
        } catch (Exception ignorada) {
            return "Conflicto al ajustar el stock";
        }
    }
}
