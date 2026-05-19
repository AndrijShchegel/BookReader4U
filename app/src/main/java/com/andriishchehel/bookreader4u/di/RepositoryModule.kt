package com.andriishchehel.bookreader4u.di

import com.andriishchehel.bookreader4u.data.repository.AuthRepository
import com.andriishchehel.bookreader4u.data.repository.AuthRepositoryImp
import com.andriishchehel.bookreader4u.data.repository.BaseRepository
import com.andriishchehel.bookreader4u.data.repository.BaseRepositoryImp
import com.andriishchehel.bookreader4u.data.repository.BookDetailsRepository
import com.andriishchehel.bookreader4u.data.repository.BookDetailsRepositoryImp
import com.andriishchehel.bookreader4u.data.repository.ReviewRepository
import com.andriishchehel.bookreader4u.data.repository.ReviewRepositoryImp
import com.andriishchehel.bookreader4u.data.repository.HomeRepository
import com.andriishchehel.bookreader4u.data.repository.HomeRepositoryImp
import com.andriishchehel.bookreader4u.data.repository.LibraryRepository
import com.andriishchehel.bookreader4u.data.repository.LibraryRepositoryImp
import com.andriishchehel.bookreader4u.data.repository.LibrarySavedRepository
import com.andriishchehel.bookreader4u.data.repository.LibrarySavedRepositoryImp
import com.andriishchehel.bookreader4u.data.repository.ReaderRepository
import com.andriishchehel.bookreader4u.data.repository.ReaderRepositoryImp
import com.andriishchehel.bookreader4u.data.repository.SearchRepository
import com.andriishchehel.bookreader4u.data.repository.SearchRepositoryImp
import com.andriishchehel.bookreader4u.data.repository.ShelfRepository
import com.andriishchehel.bookreader4u.data.repository.ShelfRepositoryImp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Provides
    @Singleton
    fun provideBaseRepository(
        auth: FirebaseAuth,
        database: FirebaseFirestore
    ): BaseRepository {
        return BaseRepositoryImp(database, auth)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        baseRepository: BaseRepository
    ): AuthRepository {
        return AuthRepositoryImp(auth, baseRepository)
    }

    @Provides
    @Singleton
    fun provideBookDetailsRepository(
        baseRepository: BaseRepository
    ): BookDetailsRepository {
        return BookDetailsRepositoryImp(baseRepository)
    }

    @Provides
    @Singleton
    fun provideReviewRepository(
        database: FirebaseFirestore,
        baseRepository: BaseRepository
    ): ReviewRepository {
        return ReviewRepositoryImp(database, baseRepository)
    }

    @Provides
    @Singleton
    fun provideHomeRepository(
        database: FirebaseFirestore
    ): HomeRepository {
        return HomeRepositoryImp(database)
    }

    @Provides
    @Singleton
    fun provideSearchRepository(
        database: FirebaseFirestore,
    ): SearchRepository {
        return SearchRepositoryImp(database)
    }

    @Provides
    @Singleton
    fun provideLibraryRepository(
        database: FirebaseFirestore,
        baseRepository: BaseRepository
    ): LibraryRepository {
        return LibraryRepositoryImp(database, baseRepository)
    }

    @Provides
    @Singleton
    fun provideLibrarySavedRepository(
        baseRepository: BaseRepository
    ): LibrarySavedRepository {
        return LibrarySavedRepositoryImp(baseRepository)
    }

    @Provides
    @Singleton
    fun provideReaderRepository(
        storage: FirebaseStorage,
        database: FirebaseFirestore,
        baseRepository: BaseRepository
    ): ReaderRepository {
        return ReaderRepositoryImp(storage, database, baseRepository)
    }

    @Provides
    @Singleton
    fun provideShelfRepository(
        database: FirebaseFirestore,
        baseRepository: BaseRepository
    ): ShelfRepository {
        return ShelfRepositoryImp(database, baseRepository)
    }
}