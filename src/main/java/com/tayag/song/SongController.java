package com.tayag.song;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "https://song-ui-j4oa.onrender.com")
@RequestMapping("/tayag/songs")
    
@RestController
@RequestMapping("/tayag/songs")
public class SongController {

    @Autowired
    private SongRepository songRepository;

    @GetMapping
    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }

    @PostMapping
    public Song addSong(@RequestBody Song song) {
        return songRepository.save(song);
    }

    @GetMapping("/{id}")
    public Song getSongById(@PathVariable Long id) {
        return songRepository.findById(id).orElseThrow(() -> new RuntimeException("Song not found"));
    }

    @PutMapping("/{id}")
    public Song updateSong(@PathVariable Long id, @RequestBody Song songDetails) {
        Song song = songRepository.findById(id).orElseThrow(() -> new RuntimeException("Song not found"));

        song.setTitle(songDetails.getTitle());
        song.setArtist(songDetails.getArtist());
        song.setAlbum(songDetails.getAlbum());
        song.setGenre(songDetails.getGenre());
        song.setUrl(songDetails.getUrl());

        return songRepository.save(song);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSong(@PathVariable Long id) {
        songRepository.deleteById(id);
        return ResponseEntity.ok("Song with ID " + id + " deleted.");
    }

    @GetMapping("/search/{keyword}")
    public List<Song> searchSongs(@PathVariable String keyword) {
        return songRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCaseOrAlbumContainingIgnoreCaseOrGenreContainingIgnoreCase(
                keyword, keyword, keyword, keyword
        );
    }
}
