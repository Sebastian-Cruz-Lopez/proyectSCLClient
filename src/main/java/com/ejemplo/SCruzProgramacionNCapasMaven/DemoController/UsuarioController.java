package com.ejemplo.SCruzProgramacionNCapasMaven.DemoController;

import com.ejemplo.SCruzProgramacionNCapasMaven.ML.Colonia;
import com.ejemplo.SCruzProgramacionNCapasMaven.ML.Direccion;
import com.ejemplo.SCruzProgramacionNCapasMaven.ML.Estado;
import com.ejemplo.SCruzProgramacionNCapasMaven.ML.Municipio;
import com.ejemplo.SCruzProgramacionNCapasMaven.ML.Result;
import com.ejemplo.SCruzProgramacionNCapasMaven.ML.ResultValidaDatos;
import com.ejemplo.SCruzProgramacionNCapasMaven.ML.Roll;
import com.ejemplo.SCruzProgramacionNCapasMaven.ML.Usuario;
import com.ejemplo.SCruzProgramacionNCapasMaven.ML.UsuarioDireccion;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @GetMapping
    public String Index(Model model) {

        try {

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Result<UsuarioDireccion>> response = restTemplate.exchange("http://localhost:8080/usuarioapi",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<UsuarioDireccion>>() {
            });

            Result<UsuarioDireccion> result = response.getBody();

            ResponseEntity<Result<Roll>> responseRolls = restTemplate.exchange("http://localhost:8080/usuarioapi/rolls",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Roll>>() {
            });

            List<Roll> resultRolls = responseRolls.getBody().objects;
;            model.addAttribute("usuariosDireccion", result.objects);
            model.addAttribute("rolls", resultRolls);

        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
        }

        return "UsuarioIndex";
    }

    @GetMapping("/direccion/estado/{idPais}")
    @ResponseBody
    public List<Estado> GetEstados(@PathVariable("idPais") int idPais) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Result<Estado>> responseGetByIdPais = restTemplate.exchange("http://localhost:8081/usuarioapi/direccion/estado/" + idPais,
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Estado>>() {
            });
            List<Estado> responseEstados = responseGetByIdPais.getBody().objects;
            return responseEstados;
        } catch (RestClientException ex) {
            System.err.println(ex);
        }
        return null;
    }

    @GetMapping("/direccion/municipio/{idEstado}")
    @ResponseBody
    public List<Municipio> GetMunicipios(@PathVariable("idEstado") int idEstado) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Result<Municipio>> responseMunicipioGetByIdEstado = restTemplate.exchange("http://localhost:8081/usuarioapi/direccion/municipio/" + idEstado,
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Municipio>>() {
            });
            List<Municipio> responseMunicipios = responseMunicipioGetByIdEstado.getBody().objects;
            return responseMunicipios;
        } catch (RestClientException ex) {
        }
        return null;
    }

    @GetMapping("/direccion/colonia/{idMunicipio}")
    @ResponseBody
    public List<Colonia> GetColonias(@PathVariable("idMunicipio") int idMunicipio) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Result<Colonia>> responseGetColoniaByIdMunicipio = restTemplate.exchange("http://localhost:8081/usuarioapi/direccion/colonia/" + idMunicipio,
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Colonia>>() {
            });

            List<Colonia> responseColonias = responseGetColoniaByIdMunicipio.getBody().objects;
            return responseColonias;
        } catch (RestClientException ex) {
            System.err.print(ex);
        }
        return null;
    }

}
