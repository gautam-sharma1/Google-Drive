package com.cfs.File_Services.controller;

import com.cfs.File_Services.model.FileEntity;
import com.cfs.File_Services.repo.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {
    @Autowired
private FileRepository fileRepository;
    private static final String UPLOAD_DIR="uploads"; // project root directory (where you run your app) we are sying by this line is “My upload folder name is uploads” Your base folder name for storing files is "uploads"

@GetMapping
   public List<FileEntity> getAllFiles(){
  return fileRepository.findAll();
    }
@GetMapping("/{id}")
    public ResponseEntity<FileEntity> getFile(@PathVariable Long id){
    // return fileRepository.findById(id).orElse(null); // old: returned null body with 200 OK when not found
    return fileRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PostMapping
    public FileEntity  createfile(@RequestBody FileEntity fileEntity){
    fileEntity.setId(null); // ignore any client-supplied id — prevents overwriting an existing row by id (IDOR)
    return  fileRepository.save(fileEntity);
    }
@DeleteMapping("/{id}")
    public ResponseEntity<Void> deletefile(@PathVariable Long id){
    if (!fileRepository.existsById(id)) {
        return ResponseEntity.notFound().build();
    }
    fileRepository.deleteById(id);
    return ResponseEntity.noContent().build();
}
@GetMapping("/folder/{folderId}")
public List<FileEntity> getFilesByFolder(@PathVariable Long folderId)
{
    return fileRepository.findByFolderId(folderId);

}

    @PostMapping("/upload")
    public Map<String,Object> uploadFile(@RequestParam("name") String name,
                                         @RequestParam("folderId") Long folderId,
                                         @RequestParam("file") MultipartFile file)
    {
        try{

            System.out.println("file name : "+ name);
            long fileSize=file.getSize();
            String fileName=file.getOriginalFilename();

            FileEntity newFile=new FileEntity();
            // newFile.setId(System.currentTimeMillis()); // old: manual timestamp id, collision-prone, id is now DB-generated via @GeneratedValue
            newFile.setName(fileName!=null ? fileName : name);
            newFile.setFolderId(folderId);
            newFile.setSize(String.valueOf(fileSize));

            // generate a safe disk filename instead of trusting the client's original filename (path traversal fix)
            String extension = "";
            if (fileName != null && fileName.contains(".")) {
                extension = fileName.substring(fileName.lastIndexOf('.'));
            }
            String storedFileName = java.util.UUID.randomUUID() + extension;

            //save file disk
            // String uploadDirPath=UPLOAD_DIR+ File.separator+newFile.getId(); // old: used manually-set id for folder name
            String uploadDirPath = UPLOAD_DIR + File.separator + folderId;
            Files.createDirectories(Paths.get(uploadDirPath));
            // Path filePath = Paths.get(uploadDirPath,newFile.getName()); // old: used client-controlled filename directly as disk filename — path traversal risk
            Path filePath = Paths.get(uploadDirPath, storedFileName);
            Files.write(filePath,file.getBytes());

            newFile.setPath(filePath.toString());

            System.out.println("dir path : "+ uploadDirPath);

            FileEntity saved=fileRepository.save(newFile);    //mysql
            return Map.of("success","true","file",saved);

        } catch (Exception e) {
            return Map.of("Success",false,"error",e.getMessage());
        }
    }
    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadFile(@PathVariable Long id)
    {
        try{
            FileEntity file=fileRepository.findById(id).orElse(null);
            if(file==null)
            {
                // return ResponseEntity.noContent().build(); // old: 204 for "not found" is semantically wrong
                return ResponseEntity.notFound().build();
            }

            // Path filePath=Paths.get(UPLOAD_DIR,id.toString(),file.getName()); // old: rebuilt path from id + client-visible filename — allowed traversal via crafted filename
            Path filePath = Paths.get(file.getPath());

            if(!Files.exists(filePath))
            {
                // return ResponseEntity.noContent().build(); // old: 204 for "not found" is semantically wrong
                return ResponseEntity.notFound().build();

            }

            byte[] fileContent=Files.readAllBytes(filePath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+file.getName() +"\"")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .header(HttpHeaders.CONTENT_LENGTH,String.valueOf(fileContent.length))
                    .body(fileContent);
        } catch (IOException e)
        {
            return ResponseEntity.internalServerError().body("Error in downloading: "+e.getMessage());
        }
    }

}


//Alright, let’s focus only on this part 👇
//
/// / save file disk
//String uploadDirPath = UPLOAD_DIR + File.separator + newFile.getId();
//Files.createDirectories(Paths.get(uploadDirPath));
//Path filePath = Paths.get(uploadDirPath, newFile.getName());
//Files.write(filePath, file.getBytes());
//🔹 1. Create folder path
//String uploadDirPath = UPLOAD_DIR + File.separator + newFile.getId();
//UPLOAD_DIR → base folder (e.g. "C:/uploads" or "uploads")
//File.separator → OS-specific (/ in Linux, \ in Windows)
//newFile.getId() → unique folder name (timestamp)
//
//👉 Example result:
//
//uploads/1713256789123
//
//So each file gets its own unique folder.
//
//🔹 2. Create directory if not exists
//Files.createDirectories(Paths.get(uploadDirPath));
//Converts string path → Path object
//Creates folder if it doesn't exist
//If already exists → no error
//
//👉 So now folder like:
//
//uploads/1713256789123/
//
//is created.
//
//🔹 3. Create full file path
//Path filePath = Paths.get(uploadDirPath, newFile.getName());
//Combines folder path + file name
//
//👉 Example:
//
//uploads/1713256789123/resume.pdf
//🔹 4. Write file to disk
//Files.write(filePath, file.getBytes());
//file.getBytes() → gets binary data of uploaded file
//Files.write(...) → saves it to the path
//
//👉 This is the actual upload happening on disk
//
//🔥 Final Flow
//Generate unique folder
//Create folder
//Build file path
//Write file bytes → saved in system
//⚠️ Important (real industry points)
//This loads full file in memory (getBytes()) → not good for large files
//👉 better: file.transferTo(filePath.toFile());
//No validation (file size, type) → risky
//File name not sanitized → security issue possible
//
//If you want, I can show you industry-level optimized version of this method (what companies actually use).
//
//i cannot understand you 4th pont if filepath contain path only then how ??????????????
//
//Good question — this is exactly where most beginners get confused.
//
//You’re thinking:
//👉 “filePath only contains path, so how does file get saved?”
//
//Let’s clear it properly.
//
//🔹 What filePath actually is
//Path filePath = Paths.get(uploadDirPath, newFile.getName());
//
//This is NOT just a folder path.
//It is the FULL destination including file name.
//
//👉 Example:
//
//uploadDirPath = uploads/1713256789123
//fileName = resume.pdf
//
//So:
//
//filePath = uploads/1713256789123/resume.pdf
//
//👉 This is the exact location where file will be created
//
//🔹 Now the important line
//Files.write(filePath, file.getBytes());
//
//Think of it like this:
//
//file.getBytes() → gives actual file data (content)
//filePath → tells WHERE to store it
//
//👉 So Java does:
//
//“Take these bytes and write them into this path”
//
//🔥 What happens internally
//
//If file does not exist:
//👉 Java creates the file automatically
//
//If file exists:
//👉 Java overwrites it
//
//🔹 Real-world analogy
//
//Think like this:
//
//filePath = empty notebook 📓 with name written on it
//file.getBytes() = content you want to write ✍️
//
//Files.write(...) =
//👉 “Write this content into that notebook”
//
//🔹 Simple Example
//Path path = Paths.get("C:/test/hello.txt");
//Files.write(path, "Hello World".getBytes());
//
//👉 Result:
//
//File hello.txt is created
//Inside it: Hello World
//🔥 Key point (IMPORTANT)
//
//👉 filePath is not just a path
//👉 It is path + file name (final destination file)
//
//That’s why Files.write() knows:
//
//where to create file
//what content to put inside
//
//If still confusing, tell me — I can draw step-by-step diagram or give debugging way so you can see it live.
//
// so it save on disk or just the data will write on the file path  ?
//
//Short answer: it saves on disk ✅
///Yes ✅ — Files.write() stores things on disk








//download files
//🔹 What this API does
//@GetMapping("/download/{id}")
//
//👉 When you hit:
//
/// api/files/download/123
//
//👉 It will:
//
//Fetch file from disk and send it to browser for download
//
//🔥 Step-by-step flow
//🔹 1. Get file from database
//FileEntity file = fileRepository.findById(id).orElse(null);
//
//👉 You are checking DB (H2):
//
//If file with this id exists → get its details
//If not → null
//🔹 2. If not found
//if(file == null)
//{
//    return ResponseEntity.noContent().build();
//}
//
//👉 If DB doesn’t have file → return empty response (HTTP 204)
//
//🔹 3. Build file path (VERY IMPORTANT)
//Path filePath = Paths.get(UPLOAD_DIR, id.toString(), file.getName());
//
//👉 Same logic as upload
//
//Example:
//
//UPLOAD_DIR = uploads
//id = 123
//fileName = resume.pdf
//
//👉 Final path:
//
//uploads/123/resume.pdf
//
//👉 This is location on disk
//
//🔹 4. Check if file exists on disk
//if(!Files.exists(filePath))
//{
//    return ResponseEntity.noContent().build();
//}
//
//👉 DB may have entry, but file might be deleted manually
//
//So:
//
//If file not present → return empty response
//🔹 5. Read file from disk
//byte[] fileContent = Files.readAllBytes(filePath);
//
//👉 This loads the file into memory as byte array
//
//Example:
//
//PDF → binary data
//Image → binary data
//🔹 6. Send response to client (IMPORTANT PART)
//return ResponseEntity.ok()
//
//Now you are building HTTP response 👇
//
//🔸 a) Force download
//.header(HttpHeaders.CONTENT_DISPOSITION,
//        "attachment; filename=\"" + file.getName() + "\"")
//
//👉 This tells browser:
//
//“Download this file instead of opening”
//
//🔸 b) Content type
//.header(HttpHeaders.CONTENT_TYPE,
//        MediaType.APPLICATION_OCTET_STREAM_VALUE)
//
//👉 Generic type → works for any file (pdf, image, etc.)
//
//🔸 c) File size
//.header(HttpHeaders.CONTENT_LENGTH,
//        String.valueOf(fileContent.length))
//
//👉 Tells browser file size
//
//🔸 d) Actual file data
//.body(fileContent);
//
//👉 This is the real file content
//
//🔥 Final Flow (easy version)
//Get file info from DB
//Build path → uploads/id/fileName
//Check file exists on disk
//Read file bytes
//Send bytes in response → browser downloads file
//🔹 Real-world analogy
//DB → gives file address 📍
//Disk → stores actual file 📄
//API → acts like courier 🚚 delivering file
//⚠️ Important (industry notes)
//readAllBytes() → bad for large files ❌
//👉 use streaming (InputStreamResource) instead
//🔥 One-line understanding
//
//👉 This API reads file from disk and sends it to user as downloadable response
//
//If you want, I can show you:
//
//optimized industry version (streaming)
//or how to open file in browser instead of download