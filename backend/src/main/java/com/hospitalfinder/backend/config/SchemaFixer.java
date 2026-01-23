package com.hospitalfinder.backend.config;

import org.springframework.stereotype.Component;

/**
 * SchemaFixer is no longer needed for MongoDB.
 * MongoDB collection and index creation is handled by MongoDbConfig.
 * This class is kept for compatibility purposes.
 */
@Component
public class SchemaFixer {
    // Removed PostgreSQL schema initialization code - no longer applicable
}
