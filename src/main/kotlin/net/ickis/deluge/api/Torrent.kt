package net.ickis.deluge.api

/**
 * Torrent information that is provided by the daemon.
 */
class Torrent(map: Map<String, Any>) {
    val trackers: List<Map<String, String>> by map
    val move_completed_path: String by map
    val paused: Boolean by map
    val compact: Boolean by map
    val upload_payload_rate: Int by map
    val peers: List<Any> by map
    val file_priorities: List<Int> by map
    val prioritize_first_last: Boolean by map
    val eta: Int by map
    val num_peers: Int by map
    val tracker_status: String by map
    val state: String by map
    val piece_length: Int by map
    val move_on_completed_path: String by map
    val move_completed: Boolean by map
    val seeds_peers_ratio: Float by map
    val max_upload_speed: Int by map
    val num_pieces: Short by map
    val max_download_speed: Int by map
    val active_time: Int by map
    val name: String by map
    val files: List<Any> by map
    val num_files: Int by map
    val time_added: Float by map
    val hash: String by map
    val next_announce: Int by map
    val private: Boolean by map
    val seeding_time: Int by map
    val seed_rank: Int by map
    val all_time_download: Int by map
    val tracker_host: String by map
    val download_payload_rate: Int by map
    val save_path: String by map
    val file_progress: List<Int> by map
    val num_seeds: Int by map
    val max_upload_slots: Int by map
    val tracker: String by map
    val move_on_completed: Boolean by map
    val stop_at_ratio: Boolean by map
    val total_payload_upload: Int by map
    val remove_at_ratio: Boolean by map
    val max_connections: Int by map
    val total_wanted: Int by map
    val stop_ratio: Float by map
    val is_auto_managed: Boolean by map
    val total_payload_download: Int by map
    val total_size: Int by map
    val total_seeds: Int by map
    val message: String by map
    val is_finished: Boolean by map
    val total_done: Int by map
    val total_peers: Int by map
    val total_uploaded: Int by map
    val progress: Float by map
    val comment: String by map
    val is_seed: Boolean by map
    val queue: Int by map
    val ratio: Float by map
    val distributed_copies: Float by map
    val test: Any by map
}
