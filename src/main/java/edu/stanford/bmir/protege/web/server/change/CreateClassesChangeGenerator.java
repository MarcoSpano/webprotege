package edu.stanford.bmir.protege.web.server.change;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import edu.stanford.bmir.protege.web.server.msg.MessageFormatter;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.semanticweb.owlapi.model.EntityType.CLASS;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 22/02/2013
 */
@AutoFactory
public class CreateClassesChangeGenerator extends AbstractCreateEntitiesChangeListGenerator<OWLClass, OWLClass> {

    @Nonnull
    private final OWLDataFactory dataFactory;

    @Inject
    public CreateClassesChangeGenerator(@Provided @Nonnull OWLDataFactory dataFactory,
                                        @Provided @Nonnull MessageFormatter msg,
                                        @Provided @Nonnull OWLOntology rootOntology,
                                        @Nonnull String sourceText,
                                        @Nonnull Optional<OWLClass> parent) {
        super(CLASS, sourceText, parent, rootOntology, dataFactory, msg);
        this.dataFactory = checkNotNull(dataFactory);
    }

    @Override
    protected Set<OWLAxiom> createParentPlacementAxioms(OWLClass freshEntity,
                                                        ChangeGenerationContext context,
                                                        Optional<OWLClass> parent) {
        if (parent.isPresent()) {
            OWLAxiom ax = dataFactory.getOWLSubClassOfAxiom(freshEntity, parent.get());
            return Collections.singleton(ax);
        }
        else {
            return Collections.emptySet();
        }
    }
}
