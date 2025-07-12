package com.ejemplo.SCruzProgramacionNCapasMaven.DemoController;

import com.ejemplo.SCruzProgramacionNCapasMaven.ML.Colonia;
import com.ejemplo.SCruzProgramacionNCapasMaven.ML.Direccion;
import com.ejemplo.SCruzProgramacionNCapasMaven.ML.Estado;
import com.ejemplo.SCruzProgramacionNCapasMaven.ML.Municipio;
import com.ejemplo.SCruzProgramacionNCapasMaven.ML.Pais;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
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
            model.addAttribute("usuariosDireccion", result.objects);
            model.addAttribute("rolls", resultRolls);

        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
        }

        return "UsuarioIndex";
    }

    @GetMapping("/delete/{idUsuario}")
    public String Delete(@PathVariable int idUsuario) {

        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.exchange("http://localhost:8080/usuarioapi/delete/" + idUsuario,
                    HttpMethod.DELETE,
                    HttpEntity.EMPTY,
                    String.class);
        } catch (Exception ex) {
            System.out.println("Error al eliminar usuario: " + ex.getLocalizedMessage());
        }

        return "redirect:/usuario";
    }

    @GetMapping("/form/{idUsuario}")
    public String Form(@PathVariable int idUsuario, Model model) {

        if (idUsuario < 1) {
            // Es agregar
            UsuarioDireccion usuarioDireccion = new UsuarioDireccion();
            usuarioDireccion.Usuario = new Usuario();
            usuarioDireccion.Direccion = new Direccion();
            usuarioDireccion.Usuario.setIdUsuario(0);

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Result<Roll>> responseRolls = restTemplate.exchange("http://localhost:8080/usuarioapi/rolls",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Roll>>() {
            });

            List<Roll> resultRolls = responseRolls.getBody().objects;

            ResponseEntity<Result<Pais>> responsePaises = restTemplate.exchange("http://localhost:8080/usuarioapi/paises",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Pais>>() {
            });

            List<Pais> resultPaises = responsePaises.getBody().objects;

            model.addAttribute("usuarioDireccion", usuarioDireccion);
            model.addAttribute("rolls", resultRolls);
            model.addAttribute("paises", resultPaises);

            return "UsuarioForm";
        } else {
            // Es edición o detalle
            RestTemplate restTemplate = new RestTemplate();
            try {
                ResponseEntity<Result<UsuarioDireccion>> responseUsuarioDireccion
                        = restTemplate.exchange("http://localhost:8080/usuarioapi/getbyid/" + idUsuario,
                                HttpMethod.GET,
                                HttpEntity.EMPTY,
                                new ParameterizedTypeReference<Result<UsuarioDireccion>>() {
                        }
                        );

                Result<UsuarioDireccion> result = responseUsuarioDireccion.getBody();

                if (result != null && result.correct) {
                    model.addAttribute("usuarioDireccion", result.object);
                    return "UsuarioDetail";
                } else {
                    model.addAttribute("error", (result != null ? result.errorMessage : "No se pudo obtener al usuario."));
                    return "UsuarioIndex"; // o la vista que prefieras
                }

            } catch (RestClientException ex) {
                model.addAttribute("error", "No se pudo conectar al servicio: " + ex.getMessage());
                return "UsuarioIndex";
            }
        }
    }

    @GetMapping("/formeditable")
    public String FormEditable(
            @RequestParam int idUsuario,
            @RequestParam(required = false, defaultValue = "-1") int idDireccion,
            Model model) {

        RestTemplate restTemplate = new RestTemplate();

        // Cargar datos del usuario con sus direcciones
        ResponseEntity<Result<UsuarioDireccion>> responseUsuarioDireccion
                = restTemplate.exchange("http://localhost:8080/usuarioapi/getbyid/" + idUsuario,
                        HttpMethod.GET,
                        HttpEntity.EMPTY,
                        new ParameterizedTypeReference<Result<UsuarioDireccion>>() {
                }
                );
        Result<UsuarioDireccion> result = responseUsuarioDireccion.getBody();

        if (result == null || !result.correct) {
            model.addAttribute("error", (result != null ? result.errorMessage : "No se pudo obtener al usuario."));
            return "UsuarioIndex";
        }

        UsuarioDireccion usuarioDireccion = result.object;

        // Si idDireccion es 0 es para agregar nueva dirección
        if (idDireccion == 0) {
            Direccion nuevaDireccion = new Direccion();
            nuevaDireccion.setIdDireccion(0); // indica nueva dirección
            usuarioDireccion.Direccion = nuevaDireccion;
        } // Si es para editar dirección existente
        else if (idDireccion > 0) {
            usuarioDireccion.Direccion = usuarioDireccion.Direcciones.stream()
                    .filter(d -> d.getIdDireccion() == idDireccion)
                    .findFirst()
                    .orElse(new Direccion());
        } // Si es -1 es para editar usuario (no toca direcciones)
        else {
            Direccion dummyDireccion = new Direccion();
            dummyDireccion.setIdDireccion(-1); // para indicar solo edición usuario
            usuarioDireccion.Direccion = dummyDireccion;
        }

        // cargar catálogos rolls y paises
        ResponseEntity<Result<Roll>> responseRolls = restTemplate.exchange(
                "http://localhost:8080/usuarioapi/rolls",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<Roll>>() {
        }
        );
        List<Roll> resultRolls = responseRolls.getBody().objects;

        ResponseEntity<Result<Pais>> responsePaises = restTemplate.exchange(
                "http://localhost:8080/usuarioapi/paises",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Result<Pais>>() {
        }
        );
        List<Pais> resultPaises = responsePaises.getBody().objects;

        model.addAttribute("usuarioDireccion", usuarioDireccion);
        model.addAttribute("rolls", resultRolls);
        model.addAttribute("paises", resultPaises);

        return "UsuarioForm";
    }

    @PostMapping("/formeditable")
    public String Accion(@Valid @ModelAttribute UsuarioDireccion usuarioDireccion,
            BindingResult bindingResult,
            Model model) {

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<UsuarioDireccion> request = new HttpEntity<>(usuarioDireccion, headers);

            Result result = new Result();

            if (usuarioDireccion.Usuario.getIdUsuario() == 0) {
                // AGREGAR usuario
                ResponseEntity<Result> response = restTemplate.exchange(
                        "http://localhost:8080/usuarioapi/adduser",
                        HttpMethod.POST,
                        request,
                        new ParameterizedTypeReference<Result>() {
                }
                );
                result = response.getBody();

            } else if (usuarioDireccion.Direccion.getIdDireccion() == -1) {
                // EDITAR usuario
                ResponseEntity<Result> response = restTemplate.exchange(
                        "http://localhost:8080/usuarioapi/updateuser",
                        HttpMethod.PUT,
                        request,
                        new ParameterizedTypeReference<Result>() {
                }
                );
                result = response.getBody();

            } else if (usuarioDireccion.Direccion.getIdDireccion() == 0) {
                // AGREGAR dirección
                ResponseEntity<Result> response = restTemplate.exchange(
                        "http://localhost:8080/usuarioapi/adddireccion",
                        HttpMethod.POST,
                        request,
                        new ParameterizedTypeReference<Result>() {
                }
                );
                result = response.getBody();

            } else {
                // EDITAR dirección
                ResponseEntity<Result> response = restTemplate.exchange(
                        "http://localhost:8080/usuarioapi/updatedireccion",
                        HttpMethod.PUT,
                        request,
                        new ParameterizedTypeReference<Result>() {
                }
                );
                result = response.getBody();
            }

            if (result.correct) {
                return "redirect:/usuario";
            } else {
                // Re-cargar catálogos para volver a mostrar el formulario con el error
                ResponseEntity<Result<Roll>> responseRolls = restTemplate.exchange(
                        "http://localhost:8080/usuarioapi/rolls",
                        HttpMethod.GET,
                        HttpEntity.EMPTY,
                        new ParameterizedTypeReference<Result<Roll>>() {
                }
                );
                List<Roll> resultRolls = responseRolls.getBody().objects;

                ResponseEntity<Result<Pais>> responsePaises = restTemplate.exchange(
                        "http://localhost:8080/usuarioapi/paises",
                        HttpMethod.GET,
                        HttpEntity.EMPTY,
                        new ParameterizedTypeReference<Result<Pais>>() {
                }
                );
                List<Pais> resultPaises = responsePaises.getBody().objects;

                model.addAttribute("error", result.errorMessage);
                model.addAttribute("usuarioDireccion", usuarioDireccion);
                model.addAttribute("rolls", resultRolls);
                model.addAttribute("paises", resultPaises);

                return "UsuarioForm";
            }

        } catch (Exception ex) {
            // También recargar catálogos en caso de excepción
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Result<Roll>> responseRolls = restTemplate.exchange(
                    "http://localhost:8080/usuarioapi/rolls",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Roll>>() {
            }
            );
            List<Roll> resultRolls = responseRolls.getBody().objects;

            ResponseEntity<Result<Pais>> responsePaises = restTemplate.exchange(
                    "http://localhost:8080/usuarioapi/paises",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Pais>>() {
            }
            );
            List<Pais> resultPaises = responsePaises.getBody().objects;

            model.addAttribute("error", "No se pudo conectar al servicio: " + ex.getMessage());
            model.addAttribute("usuarioDireccion", usuarioDireccion);
            model.addAttribute("rolls", resultRolls);
            model.addAttribute("paises", resultPaises);

            return "UsuarioForm";
        }
    }

    @GetMapping("/GetEstadosByPais/{idPais}")
    @ResponseBody
    public Result GetEstados(@PathVariable int idPais) {
        RestTemplate restTemplate = new RestTemplate();
        Result<Estado> resultEstados = new Result<>();

        try {
            ResponseEntity<Result<Estado>> responseGetByIdPais = restTemplate.exchange(
                    "http://localhost:8080/usuarioapi/GetEstadosByPais/" + idPais,
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Estado>>() {
            });

            resultEstados = responseGetByIdPais.getBody();

        } catch (RestClientException ex) {
            resultEstados.correct = false;
            resultEstados.errorMessage = ex.getLocalizedMessage();
        }
        return resultEstados;
    }

    @GetMapping("/GetMunicipiosByEstado/{idEstado}")
    @ResponseBody
    public Result GetMunicipios(@PathVariable int idEstado) {
        RestTemplate restTemplate = new RestTemplate();
        Result<Municipio> resultMunicipios = new Result<>();

        try {
            ResponseEntity<Result<Municipio>> responseMunicipios = restTemplate.exchange(
                    "http://localhost:8080/usuarioapi/GetMunicipiosByEstado/" + idEstado,
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Municipio>>() {
            });

            resultMunicipios = responseMunicipios.getBody();

        } catch (RestClientException ex) {
            resultMunicipios.correct = false;
            resultMunicipios.errorMessage = ex.getLocalizedMessage();
        }

        return resultMunicipios;
    }

    @GetMapping("/GetColoniasByMunicipio/{idMunicipio}")
    @ResponseBody
    public Result GetColonias(@PathVariable int idMunicipio) {
        RestTemplate restTemplate = new RestTemplate();
        Result<Colonia> resultColonias = new Result<>();

        try {
            ResponseEntity<Result<Colonia>> responseColonias = restTemplate.exchange(
                    "http://localhost:8080/usuarioapi/GetColoniasByMunicipio/" + idMunicipio,
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<Result<Colonia>>() {
            });

            resultColonias = responseColonias.getBody();

        } catch (RestClientException ex) {
            resultColonias.correct = false;
            resultColonias.errorMessage = ex.getLocalizedMessage();
        }

        return resultColonias;
    }

}
